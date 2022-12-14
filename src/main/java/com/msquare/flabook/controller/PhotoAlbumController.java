package com.msquare.flabook.controller;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import com.msquare.flabook.common.controllers.CommonResponse;
import com.msquare.flabook.common.controllers.CommonResponseCode;
import com.msquare.flabook.dto.PhotoAlbumDto;
import com.msquare.flabook.dto.PostingDto;
import com.msquare.flabook.dto.ShareDto;
import com.msquare.flabook.form.CreatePhotoVo;
import com.msquare.flabook.json.Views;
import com.msquare.flabook.models.User;
import com.msquare.flabook.service.*;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("java:S4684")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/photoAlbums")
public class PhotoAlbumController {

    private final PhotoAlbumService photoAlbumService;
    private final UserService userService;
    private final LikePostingService likePostingService;
    private final ShareService shareService;
    private final PhotoRecentWriterService photoRecentWriterService;

    @GetMapping("")
    public CommonResponse<List<PhotoAlbumDto>> getPhotoAlbum(@ApiIgnore User currentUser, @RequestParam(value = "userId", required = false) Long userId){
        userId = Objects.isNull(userId) ? currentUser.getId() : userId;
        return new CommonResponse<>(CommonResponseCode.SUCCESS.getResultCode(), photoAlbumService.getPhotoAlbumByUserId(userId));
    }

    @PostMapping
    public CommonResponse<PhotoAlbumDto> createPhotoAlbum(@ApiIgnore User currentUser, @RequestParam("name") String name){
        return new CommonResponse<>(CommonResponseCode.SUCCESS.getResultCode(), PhotoAlbumDto.of(photoAlbumService.createPhotoAlbum(currentUser, name)));
    }

    @PutMapping("/{albumId}")
    public CommonResponse<Boolean> updatePhotoAlbum(@ApiIgnore User currentUser, @PathVariable("albumId") Long albumId, @RequestParam("name") String name){
        photoAlbumService.updatePhotoAlbum(currentUser, albumId, name);
        return new CommonResponse<>(CommonResponseCode.SUCCESS.getResultCode(), true);
    }

    @DeleteMapping("/{albumId}")
    public CommonResponse<Boolean> deletePhotoAlbum(@ApiIgnore User currentUser, @PathVariable("albumId") Long albumId){
        photoAlbumService.deletePhotoAlbum(currentUser, albumId);
        userService.userIndexing(currentUser.getId());
        return new CommonResponse<>(CommonResponseCode.SUCCESS.getResultCode(), true);
    }

    /**
     * ?????? ?????? : (????????? ???????????? ???????????? ???)
     * */
    @GetMapping("/{albumId}")
    public CommonResponse<List<PostingDto>> getPhotos(@ApiIgnore User currentUser, @PathVariable("albumId") Long albumId, @RequestParam(required = false) Long sinceId,@RequestParam(required = false) Long maxId, @RequestParam(required = false, defaultValue = "10") int count, @RequestParam(required = false, defaultValue = "id") String orderby, @RequestParam(required = false, defaultValue = "0") Integer offset){
        List<PostingDto> photosByUserAndAlbum = photoAlbumService.getPhotosByUserAndAlbum(albumId,  sinceId, maxId, count, orderby, offset);
        photosByUserAndAlbum.forEach(postingDto -> {
            boolean isLike = likePostingService.isLikePost(postingDto.getId(), currentUser);
            postingDto.setIsLike(isLike);
        });
        return new CommonResponse<>(CommonResponseCode.SUCCESS.getResultCode(), photosByUserAndAlbum);
    }

    @GetMapping("/{albumId}/photoCount")
    public CommonResponse<Integer> getPhotosCount(@ApiIgnore User currentUser, @PathVariable("albumId") Long albumId){
        return new CommonResponse<>(CommonResponseCode.SUCCESS.getResultCode(), photoAlbumService.getPhotoAlbumById(albumId).getPhotoCnt());
    }

    private final PostingService postingService;
    //?????? ???????????? ?????? ??????
    @JsonView(Views.BaseView.class)
    @PostMapping(path = "/me/{albumId}")
    public CommonResponse<List<PostingDto>> doCreatePhotoPosting(@Valid CreatePhotoVo createPhotoVo, @ApiIgnore User currentUser, @PathVariable("albumId") Long albumId) {
        List<PostingDto> postingDtos = postingService.createPhotoPosting(createPhotoVo, currentUser, true);
        userService.userIndexing(currentUser.getId());
        return new CommonResponse<>(CommonResponseCode.SUCCESS.getResultCode(), postingDtos, "test");
    }

    /**
     * ????????????????????? ????????? ???????????? (4???) - ?????????
     * */

    @GetMapping("/representPhotos")
    public CommonResponse<List<PostingDto>> doFindRepresentPhotos(@ApiIgnore User currentUser, @RequestParam(value = "userId", required = false) Long userId){
        userId = Objects.isNull(userId) ? currentUser.getId() : userId;
        List<PostingDto> postingDtoList = photoAlbumService.findUsersRepresentPhotos(userId);
        postingDtoList.forEach(postingDto -> {
            boolean isLike = likePostingService.isLikePost(postingDto.getId(), currentUser);
            postingDto.setIsLike(isLike);
        });
        return new CommonResponse<>(CommonResponseCode.SUCCESS.getResultCode(), postingDtoList);
    }


    /**
     * ???????????? : ?????? ?????? ???????????? ????????? ?????? ?????? ???, ????????? ????????? share???. ???????????? ?????? ????????? owner??? ????????? album?????? ????????????????????? ???
     * */
    @JsonView(Views.BaseView.class)
    @PostMapping(path = "/{userId}/share")
    public CommonResponse<ShareDto> doShare(@ApiIgnore User owner, @PathVariable("userId") Long userId) {
        return new CommonResponse<>(CommonResponseCode.SUCCESS.getResultCode(), shareService.doSharePhotoAlbum(owner, userId));
    }

    @GetMapping("/photoRecentIndexing/{id}")
    public void check(@ApiIgnore User currentUser, @PathVariable("id") Long id){
        photoRecentWriterService.photoRecentWriterIndexing(id);
    }
}

