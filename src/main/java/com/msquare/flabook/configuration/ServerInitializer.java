package com.msquare.flabook.configuration;

import com.drew.imaging.ImageProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.msquare.flabook.common.configurations.S3Bucket;
import com.msquare.flabook.models.Badge;
import com.msquare.flabook.repository.BadgeRepository;
import com.msquare.flabook.service.FolderDatePatterns;
import com.msquare.flabook.service.ImageUploadService;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServerInitializer implements ApplicationRunner {

    private static final String DELIMITER = "/";
    private static final String BOOLEAN_FALSE = "false";

    private final S3Bucket bucket;
    private final ResourceLoader resourceLoader;

    private final BadgeRepository badgeRepository;
    private final ImageUploadService imageUploadService;

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        initApplication();
    }

    @SuppressWarnings("unused")
    private void recursive(Resource resource,  String prefix,  List<Resource> container) throws IOException {
        if(!resource.isFile()) {
            log.info("resource {}", resource.getURI().getPath());
            String path = (prefix != null && prefix.length() > 0) ? prefix + DELIMITER + resource.getFile().getName() :  resource.getFile().getName() ;

            String classPath = "classpath:" +  path + "/*";
            log.info("path : {}, classpath : {}", path, classPath);
            Resource[] resources = ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources(classPath);
            for(Resource r : resources) {
                recursive(r, path,  container);
            }
        } else if(resource.isFile()){
            container.add(resource);
        }
    }

    private void initImageResource() throws IOException, InterruptedException {
        ResourcePatternResolver resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
        Resource[] resources = resourcePatternResolver.getResources("classpath*:/static/**/*");

        for(Resource resource :resources) {
            if(!resource.isReadable()) continue;

            String path = resource.getURI().toString();
            path = path.replace(File.separator, "/");
            String key = path.substring(path.indexOf("/static") + 8);
            String prefix = key.substring(0, key.lastIndexOf("/"));
            String filename = resource.getFilename();
            if(bucket.isExists(key)) {
                log.info("exists commons uri : {}, prefix : {}, filename :{}", key, prefix, resource.getFilename());
            } else {
                log.info("upload commons uri : {}, prefix : {}, filename :{}", key, prefix, resource.getFilename());
                bucket.upload(prefix, resource.getInputStream(), filename);
            }
        }
    }

    private void initBadge() {
        if(badgeRepository.count() == 0L) {

            List<String[]> names = Arrays.asList( //?????? ????????????
                    new String[]{"?????? ??????", "?????? ????????? ???????????????!", "???????????? ????????? ??????????????? ???????????? ?????? ???????????????!", "static/commons/badges/badges_active/????????????_active.png", "static/commons/badges/badges_inactive/????????????_inactive.png"},
                    new String[]{"????????? ??????", "????????? ????????? ???????????????!", "????????? ???????????? ???????????? ?????? ????????? ?????? ???????????????!", "static/commons/badges/badges_active/???????????????_active.png", "static/commons/badges/badges_inactive/???????????????_inactive.png"},
                    new String[]{"?????? ??????", "?????? ????????? ????????? ???????????????!", "????????? ???????????? ?????? ?????? ????????? ??? ??????! ????????????!", "static/commons/badges/badges_active/????????????_active.png", "static/commons/badges/badges_inactive/????????????_inactive.png"},

                    new String[]{"?????? ?????????", "????????? ???????????? ????????? ????????? ???????????????!", "?????? ???????????? ????????? ???????????? ??????????????????!", "static/commons/badges/badges_active/???????????????_active.png", "static/commons/badges/badges_inactive/???????????????_inactive.png", BOOLEAN_FALSE},
                    new String[]{"?????? ????????????", "????????? ???????????? ???????????? ????????? ???????????????!", "????????? ???????????? ?????? ?????????!", "static/commons/badges/badges_active/??????????????????_active.png", "static/commons/badges/badges_inactive/??????????????????_inactive.png", BOOLEAN_FALSE},
                    new String[]{"?????? ????????????", "????????? ???????????? ???????????? ????????? ???????????????!", "???????????? ????????? ?????? ???????????? ?????? ?????????!", "static/commons/badges/badges_active/??????????????????_active.png", "static/commons/badges/badges_inactive/??????????????????_inactive.png", BOOLEAN_FALSE},

                    new String[]{"?????? ??????", "????????????????????? 10??? ?????? ???????????????!", "???????????? ????????? ???????????? ??????????????????!", "static/commons/badges/badges_active/????????????_active.png", "static/commons/badges/badges_inactive/????????????_inactive.png"},
                    new String[]{"?????? ??????", "????????????????????? 100??? ?????? ???????????????!", "???????????? ?????? ????????? ????????? ???????????????!", "static/commons/badges/badges_active/????????????_active.png", "static/commons/badges/badges_inactive/????????????_inactive.png"},
                    new String[]{"?????? ??????", "????????????????????? 500??? ????????? ???????????????!", "????????? ?????? ???????????? ???????????????!", "static/commons/badges/badges_active/????????????_active.png", "static/commons/badges/badges_inactive/????????????_inactive.png"},

                    new String[]{"??? ????????? ??????", "??????????????? ??? ????????? ???????????????!", "???????????? ??????????????? ????????? ?????? ???????????????!", "static/commons/badges/badges_active/??????????????????_active.png", "static/commons/badges/badges_inactive/??????????????????_inactive.png"},

                    new String[]{"????????? ?????????", "????????? ??? ????????? ???????????? ?????????????", "???????????? ????????? ????????? ????????? ??????????????????!", "static/commons/badges/badges_active/??????????????????_active.png", "static/commons/badges/badges_inactive/??????????????????_inactive.png"},
                    new String[]{"????????? ??????", "????????? ?????? ?????? ???????????? ??????????", "???????????? ????????? ?????? ?????? ??????????????????!", "static/commons/badges/badges_active/???????????????_active.png", "static/commons/badges/badges_inactive/???????????????_inactive.png"},
                    new String[]{"????????? ???", "??????????????? ?????? ?????? ????????????!", "????????? ????????? ????????? ????????? ???????????????!", "static/commons/badges/badges_active/????????????_active.png", "static/commons/badges/badges_inactive/????????????_inactive.png"},

                    new String[]{"??????", "???????????? ?????? ??????!", "????????? ???????????????!", "static/commons/badges/badges_active/??????_active.png", "static/commons/badges/badges_inactive/??????_inactive.png"},

                    new String[]{"?????????", "?????????????????? ????????? ????????? ????????? ??????!", "???????????? ??????????????????!", "static/commons/badges/badges_active/?????????_active.png", "static/commons/badges/badges_inactive/?????????_inactive.png"},

                    new String[]{"?????? ??????", "????????? ???????????? ?????? ????????? ??????????????????!", "????????? ?????? ??????????????? ????????????!", "static/commons/badges/badges_active/????????????_active.png", "static/commons/badges/badges_inactive/????????????_inactive.png"},
                    new String[]{"?????? ??????", "????????? ???????????? ?????? ????????? ??????????????????!", "????????? ??? ????????? ?????? ????????? ??????????????????!", "static/commons/badges/badges_active/????????????_active.png", "static/commons/badges/badges_inactive/????????????_inactive.png"},
                    new String[]{"?????? ??????", "??????????????? ????????????! ?????? ????????? ??????????????????!", "????????? ?????? ?????? ?????????!", "static/commons/badges/badges_active/????????????_active.png", "static/commons/badges/badges_inactive/????????????_inactive.png"},

                    new String[]{"????????? ??????", "????????????????????? ????????? ????????? ??????????????????!", "????????? ?????? ??????????????? ????????????!", "static/commons/badges/badges_active/???????????????_active.png", "static/commons/badges/badges_inactive/???????????????_inactive.png"},
                    new String[]{"????????? ??????", "????????????????????? ????????? ????????? ??????????????????!", "????????? ??? ????????? ????????? ????????? ??????????????????!", "static/commons/badges/badges_active/???????????????_active.png", "static/commons/badges/badges_inactive/???????????????_inactive.png"},
                    new String[]{"????????? ??????", "??????????????? ????????????! ????????? ????????? ??????????????????!", "????????? ?????? ????????? ?????????!", "static/commons/badges/badges_active/???????????????_active.png", "static/commons/badges/badges_inactive/???????????????_inactive.png"},

                    new String[]{"???????????????", "????????? ??? ????????? ?????? ??????????????? ????????????!", "????????? ???????????? ?????????????????????!", "static/commons/badges/badges_active/???????????????_active.png", "static/commons/badges/badges_inactive/???????????????_inactive.png"},
                    new String[]{"?????? ??????", "?????? ??? ????????? ??? ????????? ???????????????!", "???????????? ??????????????? ???????????????!", "static/commons/badges/badges_active/????????????_active.png", "static/commons/badges/badges_inactive/????????????_inactive.png"},
                    new String[]{"????????????", "????????? 100??? ????????? ??????!", "???????????? ????????? ????????????!", "static/commons/badges/badges_active/????????????_active.png", "static/commons/badges/badges_inactive/????????????_inactive.png"},

                    new String[]{"????????????", "???????????? ???????????? ??????!", "????????? ????????? ????????? ?????? ????????????!", "static/commons/badges/badges_active/????????????_active.png", "static/commons/badges/badges_inactive/????????????_inactive.png"},

                    new String[]{"????????????", "???????????? ???????????? ????????? ??????!", "????????? ???????????? ?????????????????????!", "static/commons/badges/badges_active/????????????_active.png", "static/commons/badges/badges_inactive/????????????_inactive.png",BOOLEAN_FALSE},

                    new String[]{"?????????", "????????? ????????? ????????? ??????!", "????????? ?????? ??????????????????!", "static/commons/badges/badges_active/?????????_active.png", "static/commons/badges/badges_inactive/?????????_inactive.png"},
                    new String[]{"????????????", "?????? ????????? ????????? ????????? ??????!", "???????????? ???????????????^^", "static/commons/badges/badges_active/????????????_active.png", "static/commons/badges/badges_inactive/????????????_inactive.png"}

            );
            AtomicInteger idx = new AtomicInteger(1);

            List<Badge> entities = names.stream().map( texts -> {
                String title = texts[0];
                String des1 = texts[1];
                String des2 = texts[2];
                String truePath = texts[3];
                String falsePath = texts[4];

                Badge badge = new Badge();
                badge.setTitle(title);
                badge.setDescription1(des1);
                badge.setDescription2(des2);
                badge.setActive(texts.length <= 5);
                badge.setOrderCount(idx.getAndIncrement());

                try {
                    ClassPathResource trueResource = new ClassPathResource(truePath);
                    ClassPathResource falseResource = new ClassPathResource(falsePath);

                    File trueFile = new File(truePath);
                    FileItem trueFileItem = new DiskFileItem(StringUtils.replace(title," ","")+"_active", Files.probeContentType(trueFile.toPath()), false, trueFile.getName(), (int) trueFile.length(), trueFile.getParentFile());
                    IOUtils.copy(trueResource.getInputStream(), trueFileItem.getOutputStream());
                    MultipartFile trueMultipartFile = new CommonsMultipartFile(trueFileItem);

                    File falseFile = new File(falsePath);
                    FileItem falseFileItem = new DiskFileItem(StringUtils.replace(title," ","")+"_inactive", Files.probeContentType(falseFile.toPath()), false, falseFile.getName(), (int) falseFile.length(), falseFile.getParentFile());
                    IOUtils.copy(falseResource.getInputStream(), falseFileItem.getOutputStream());
                    MultipartFile falseMultipartFile = new CommonsMultipartFile(falseFileItem);

                    ImageUploadService.ImageResourceInfoWithMetadata trueImageResource = imageUploadService.uploadWithMeta(FolderDatePatterns.BADGES, trueMultipartFile);
                    ImageUploadService.ImageResourceInfoWithMetadata falseImageResource = imageUploadService.uploadWithMeta(FolderDatePatterns.BADGES, falseMultipartFile);

                    badge.setTrueImageResource(trueImageResource.getImageResource());
                    badge.setFalseImageResource(falseImageResource.getImageResource());

                } catch (ImageProcessingException | IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }

                return badge;
            }).collect(Collectors.toList());

            badgeRepository.saveAll(entities);
        }
    }

    private void initApplication() throws IOException, InterruptedException {
        log.info("initApplication");
        initImageResource();
        initBadge();
    }


}
