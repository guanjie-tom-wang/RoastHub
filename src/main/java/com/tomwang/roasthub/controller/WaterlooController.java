package com.tomwang.roasthub.controller;

import com.tomwang.roasthub.dao.pojo.RecipeItem;
import com.tomwang.roasthub.dao.pojo.Users;
import com.tomwang.roasthub.service.LoginService;
import com.tomwang.roasthub.service.WaterlooService;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;

@RestController
@CrossOrigin(origins = "http://localhost:8080")
@RequestMapping("/waterloo")
public class WaterlooController {
    @Autowired
    private WaterlooService waterlooService;
    @Autowired
    private LoginService loginService;

    @Autowired
    private RedisTemplate<String, byte[]> redisTemplate;
    private static final Logger logger = LoggerFactory.getLogger(WaterlooController.class);

    @Autowired
    public WaterlooController(WaterlooService waterlooService) {
        this.waterlooService = waterlooService;
    }
//    @PostMapping("/upload")
//    public ResponseEntity<List<String>> uploadFile(@RequestParam("image") MultipartFile image, @RequestParam("file") MultipartFile file, @RequestParam("name") String name, String type) {
//        try {
//            List<String> fileIds = waterlooService.storeFiles(image, file,name, type);
//            logger.debug("Uploading file successfully");
//            return ResponseEntity.ok(fileIds);
//        } catch (IOException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//        }
//    }
@PostMapping("/upload")
public ResponseEntity<?> uploadFile(
        @RequestParam("image") MultipartFile image,
        @RequestParam("file") MultipartFile file,
        @RequestParam("name") String name,
        @RequestParam("token") String token, // 添加 token 参数
        String type) {
    logger.info("Uploading: " + token);
    // 验证 token
    if(token.equals("null")){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户未登录或令牌无效");

    }
    Users user = loginService.checkToken(token);
    if (user == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户未登录或令牌无效");
    }

    // 用户已验证，继续上传逻辑
    try {
        List<String> fileIds = waterlooService.storeFiles(image, file, name, type, user.getUsername());
        logger.debug("Uploading file successfully");
        return ResponseEntity.ok(fileIds);
    } catch (IOException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
}

    @GetMapping("/list")
    public List<GridFSFile> listFiles() {
        return waterlooService.listFiles();
    }
    @GetMapping("/description/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String id) {
        logger.info("Description file with ID: {}", id);
        try {
            // Try to get the file from the Redis cache
            byte[] fileContent = redisTemplate.opsForValue().get("files:" + id);
            String contentType = "application/pdf"; // default content type

            if (fileContent == null) {
                GridFSFile gridFSFile = waterlooService.getFile(new ObjectId(id));
                if (gridFSFile == null) {
                    logger.info("File not found for ID: {}", id);
                    return ResponseEntity.notFound().build();
                }

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                waterlooService.downloadStream(gridFSFile.getObjectId(), outputStream);
                fileContent = outputStream.toByteArray();

                // Update content type if available
                String metadataContentType = Optional.ofNullable(gridFSFile.getMetadata())
                        .map(metadata -> metadata.getString("_contentType"))
                        .orElse(contentType);
                contentType = metadataContentType;

                // Store the file in the Redis cache
                redisTemplate.opsForValue().set("files:" + id, fileContent);
            }

            InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(fileContent));

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + id + "\"") // Filename could be cached as well
                    .body(resource);
        } catch (Exception e) {
            logger.error("Error occurred while downloading file with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    @GetMapping("/showimage/{id}")
    public ResponseEntity<Resource> showImage(@PathVariable String id) {
        logger.info("Description file with ID: {}", id);
        try {
            // Try to get the file from the Redis cache
            byte[] fileContent = redisTemplate.opsForValue().get("files:" + id);
            String contentType = "image/jpeg"; // default content type

            if (fileContent == null) {
                GridFSFile gridFSFile = waterlooService.getFile(new ObjectId(id));
                if (gridFSFile == null) {
                    logger.info("File not found for ID: {}", id);
                    return ResponseEntity.notFound().build();
                }

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                waterlooService.downloadStream(gridFSFile.getObjectId(), outputStream);
                fileContent = outputStream.toByteArray();

                // Update content type if available
                String metadataContentType = Optional.ofNullable(gridFSFile.getMetadata())
                        .map(metadata -> metadata.getString("_contentType"))
                        .orElse(contentType);
                contentType = metadataContentType;

                // Store the file in the Redis cache
                redisTemplate.opsForValue().set("files:" + id, fileContent);
            }

            InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(fileContent));

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + id + "\"") // Filename could be cached as well
                    .body(resource);
        } catch (Exception e) {
            logger.error("Error occurred while downloading file with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/show")
    public ResponseEntity<List<RecipeItem>> getBreakfastItems() {
        List<GridFSFile> files = waterlooService.getRandomFiles(1000);
        logger.info("files: "+files);

        List<RecipeItem> recipeItems = new ArrayList<>();

        for (GridFSFile file : files) {
            String filename = file.getMetadata().getString("name");
            String fileType = file.getMetadata().getString("type");
            String itemName = file.getMetadata().getString("itemName");
            if (itemName == null) {
                logger.warn("Item name is null for file: " + filename);
                continue;
            }

            ObjectId pdfId = waterlooService.findPdfIdByName(itemName);
            ObjectId imageId = waterlooService.findImageIdByName(itemName);

            if (pdfId == null || imageId == null) {
                logger.warn("pdfId or imageId is null for item: " + itemName);
                continue;
            }

            RecipeItem recipeItem = new RecipeItem();
            recipeItem.setDetailUrl(pdfId.toString());
            recipeItem.setType(fileType);
            recipeItem.setPictureUrl(imageId.toString());
            recipeItem.setName(filename); // 使用 itemName 作为 BreakfastItem 的名称
            recipeItems.add(recipeItem);
        }

        return ResponseEntity.ok(recipeItems);
    }
}
