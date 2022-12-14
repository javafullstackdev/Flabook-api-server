package com.msquare.flabook.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.msquare.flabook.common.controllers.CommonResponse;
import com.msquare.flabook.common.controllers.CommonResponseCode;
import com.msquare.flabook.dto.BadgeDto;
import com.msquare.flabook.dto.UserWithBadgeDto;
import com.msquare.flabook.models.User;
import com.msquare.flabook.models.UserBadge;
import com.msquare.flabook.service.BadgeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

@SuppressWarnings({"java:S4684", "unused"})
@Slf4j
@RestController
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;

    /**
     * badge CRUD
     * */
    @PostMapping("/v2/home/badge")
    public void createBadge(@RequestParam("title") String title,
                            @RequestParam("description1") String description1,
                            @RequestParam("description2") String description2,
                            @RequestParam("trueImage") MultipartFile trueImage,
                            @RequestParam("falseImage") MultipartFile falseImage){
        badgeService.createBadge(title,description1, description2, trueImage, falseImage);
    }

    @GetMapping("/v2/home/badge/{id}")
    public BadgeDto getBadge(@PathVariable("id") long id){
        return badgeService.getBadge(id);
    }

    @PutMapping("/v2/home/badge/{id}")
    public void updateBadge(@PathVariable("id") long id,
                            @RequestParam("title") String title,
                            @RequestParam("description1") String description1,
                            @RequestParam("description2") String description2){
        badgeService.updateBadge(id, title,description1, description2);
    }

    @DeleteMapping("/v2/home/badge/{id}")
    public void deleteBadge(@PathVariable("id") long id){
        badgeService.deleteBadge(id);
    }


    /**
     * ?????? ?????? ??????
     * */
    @GetMapping("/v2/users/me/badges")
    public CommonResponse<UserWithBadgeDto> doFindMyBadges(@ApiIgnore User currentUser){
        return new CommonResponse<>(CommonResponseCode.SUCCESS, badgeService.getUserBadgeDto(currentUser.getId()));
    }

    /**
     * represent badge : ?????? ?????? ??????
     * */
    @PostMapping("/v2/users/me/badges/representBadge")
    public CommonResponse<UserWithBadgeDto> updateRepresentBadge(@ApiIgnore User currentUser, String id ){
        if(StringUtils.isEmpty(id)){
            badgeService.deleteUserRepresentBadge(currentUser);
            return new CommonResponse<>(CommonResponseCode.SUCCESS, badgeService.getUserBadgeDto(currentUser.getId()));
        }
        if(badgeService.setRepresentBadge(currentUser, Long.parseLong(id)))
            return new CommonResponse<>(CommonResponseCode.SUCCESS, badgeService.getUserBadgeDto(currentUser.getId()));
        return new CommonResponse<>(CommonResponseCode.FAIL.getResultCode(), null,"?????? ????????? ????????? ????????? ????????? ????????? ????????????.");
    }

    /**
     * ?????? ???????????? ??????????????? ??????
     * */
    @GetMapping("/v2/users/{id}/badges")
    public CommonResponse<UserWithBadgeDto> doFindUserBadges(@ApiIgnore User currentUser, @PathVariable Long id){
        return new CommonResponse<>(CommonResponseCode.SUCCESS, badgeService.getUserBadgeDto(id));
    }


    /**
     * user??? mybadges??? ?????? ?????? : ????????? ?????? user??? ?????? ?????? ????????? ?????? => ????????? ?????????x
     * */
    @PostMapping("/v2/users/me/badges/{id}/addMyBadges")
    public UserWithBadgeDto addUserBadges(@ApiIgnore User currentUser, @PathVariable long id) {
        badgeService.addUserBadges(currentUser.getId(), id);
        return badgeService.getUserBadgeDto(currentUser.getId());
    }

    /**
     * ?????? user??? mybadges??? ?????? ?????? : ?????? userId?????? ?????? badge??? ??????????????? ??????
     * */
    @PostMapping("/v2/users/{userId}/badges/{badgeId}/addMyBadges")
    public UserWithBadgeDto addUserBadges(@PathVariable long userId, @PathVariable long badgeId) {
        badgeService.addUserBadges(userId, badgeId);
        return badgeService.getUserBadgeDto(userId);
    }

    /**
     * user????????? ?????? badge ??????
     * */
    @DeleteMapping("/v2/users/me/badges/{id}/deleteMyBadges")
    public UserWithBadgeDto deleteUserBadges(@ApiIgnore User currentUser, @PathVariable long id) {
        badgeService.deleteUserBadges(currentUser, id);
        return badgeService.getUserBadgeDto(currentUser.getId());
    }


    @PostMapping("/v2/users/me/badges/clear")
    public void clear(@ApiIgnore User currentUser) {
        badgeService.clear(currentUser);
    }

    @GetMapping("/v2/users/me/badges/count")
    public String show(@ApiIgnore User currentUser) {
        StringBuilder builder = new StringBuilder();

        builder.append("currentUser.getBadgeCount() = ");
        builder.append(currentUser.getBadgeCount());
        builder.append("\r\n<br>");

        for(UserBadge uv : currentUser.getMyBadges()) {
            builder.append("uv.getBadge().getTitle() = ");
            builder.append(uv.getBadge().getTitle());
            builder.append("\r\n<br>");
        }

        builder.append("currentUser.getMyBadges().size() = ");
        builder.append(currentUser.getMyBadges().size());

        return builder.toString();
    }
}
