package com.tomwang.roasthub.controller;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.mysql.cj.log.Log;
import com.tomwang.roasthub.dao.pojo.RecipeItem;
import com.tomwang.roasthub.dao.pojo.Users;
import com.tomwang.roasthub.service.*;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
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

@RestController
@RequestMapping("/myUpload")
public class UploadHistoryController {
    @Autowired
    private KitchenerService kitchenerService;
    @Autowired
    private WaterlooService waterlooService;
    @Autowired
    private TorontoService torontoService;
    @Autowired
    private MississaugaService mississaugaService;

    @Autowired
    private LoginService loginService;
    @Autowired
    private RedisTemplate<String, byte[]> redisTemplate;
    private static final Logger logger = LoggerFactory.getLogger(UploadHistoryController.class);
    @GetMapping("/description/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String id) {
        logger.info("Description file with ID: {}", id);
        try {
            // Try to get the file from the Redis cache
            byte[] fileContent = redisTemplate.opsForValue().get("files:" + id);
            String contentType = "application/pdf"; // default content type

            if (fileContent == null) {
                GridFSFile gridFSFile = kitchenerService.getFile(new ObjectId(id));
                if (gridFSFile == null) {
                    logger.info("File not found for ID: {}", id);
                    return ResponseEntity.notFound().build();
                }

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                kitchenerService.downloadStream(gridFSFile.getObjectId(), outputStream);
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

    @GetMapping("/show/{token}")
    public ResponseEntity<List<RecipeItem>> getBreakfastItems(@PathVariable String token) {
        Users user = loginService.checkToken(token);
        logger.info("user" + user);


        List<GridFSFile> kitchenerFile = kitchenerService.listFiles();
        List<GridFSFile> waterlooFile = waterlooService.listFiles();
        List<GridFSFile> torontoFile = torontoService.listFiles();
        List<GridFSFile> mississaugaFile = mississaugaService.listFiles();

        List<GridFSFile> files = new ArrayList<>(kitchenerFile);
        files.addAll(waterlooFile);
        files.addAll(torontoFile);
        files.addAll(mississaugaFile);

        List<RecipeItem> recipeItems = new ArrayList<>();

        for (GridFSFile file : files) {
            assert file.getMetadata() != null;
            String username = file.getMetadata().getString("userName");
            logger.info("username" + username);
            if(user.getUsername().equals(username)) {
                String filename = file.getMetadata().getString("name");
                String fileType = file.getMetadata().getString("type");
                String itemName = file.getMetadata().getString("itemName");
                if (itemName == null) {
                    logger.warn("Item name is null for file: " + filename);
                    continue;
                }
                ObjectId pdfId = null;
                ObjectId imageId = null;
                if(kitchenerService.findPdfIdByName(itemName) != null) {
                    pdfId = kitchenerService.findPdfIdByName(itemName);
                }
                if(waterlooService.findPdfIdByName(itemName) != null) {
                    pdfId = waterlooService.findPdfIdByName(itemName);
                }
                if(torontoService.findPdfIdByName(itemName) != null) {
                    pdfId = torontoService.findPdfIdByName(itemName);
                }
                if(mississaugaService.findPdfIdByName(itemName) != null) {
                    pdfId = mississaugaService.findPdfIdByName(itemName);
                }
                if(kitchenerService.findImageIdByName(itemName) != null) {
                    imageId = kitchenerService.findImageIdByName(itemName);
                }
                if(waterlooService.findImageIdByName(itemName) != null) {
                    imageId = waterlooService.findImageIdByName(itemName);
                }
                if(torontoService.findImageIdByName(itemName) != null) {
                    imageId = torontoService.findImageIdByName(itemName);
                }
                if(mississaugaService.findImageIdByName(itemName) != null) {
                    imageId = mississaugaService.findImageIdByName(itemName);
                }

                if (pdfId == null || imageId == null) {
                    logger.warn("pdfId or imageId is null for item: " + itemName);
                    continue;
                }
                RecipeItem recipeItem = new RecipeItem();
                recipeItem.setDetailUrl(pdfId.toString());
                recipeItem.setType(fileType);
                recipeItem.setPictureUrl(imageId.toString());
                recipeItem.setName(filename);
                recipeItem.setItemId(itemName);
                if(!recipeItems.contains(recipeItem))
                    recipeItems.add(recipeItem);
            }
            logger.info("Recipe length" + recipeItems.size());
        }

        return ResponseEntity.ok(recipeItems);
    }
    @DeleteMapping("/delete/{itemName}")
    public void delete(@PathVariable String itemName) {
        ObjectId pdfId = null;
        ObjectId imageId = null;
        if(kitchenerService.findPdfIdByName(itemName) != null) {
            pdfId = kitchenerService.findPdfIdByName(itemName);
            kitchenerService.delete(pdfId);
        }
        if(waterlooService.findPdfIdByName(itemName) != null) {
            pdfId = waterlooService.findPdfIdByName(itemName);
            waterlooService.delete(pdfId);
        }
        if(torontoService.findPdfIdByName(itemName) != null) {
            pdfId = torontoService.findPdfIdByName(itemName);
            torontoService.delete(pdfId);
        }
        if(mississaugaService.findPdfIdByName(itemName) != null) {
            pdfId = mississaugaService.findPdfIdByName(itemName);
            mississaugaService.delete(pdfId);
        }
        if(kitchenerService.findImageIdByName(itemName) != null) {
            imageId = kitchenerService.findImageIdByName(itemName);
            kitchenerService.delete(imageId);
        }
        if(waterlooService.findImageIdByName(itemName) != null) {
            imageId = waterlooService.findImageIdByName(itemName);
            waterlooService.delete(imageId);

        }
        if(torontoService.findImageIdByName(itemName) != null) {
            imageId = torontoService.findImageIdByName(itemName);
            torontoService.delete(imageId);
        }
        if(mississaugaService.findImageIdByName(itemName) != null) {
            imageId = mississaugaService.findImageIdByName(itemName);
            mississaugaService.delete(imageId);
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
                GridFSFile gridFSFile = kitchenerService.getFile(new ObjectId(id));
                if (gridFSFile == null) {
                    logger.info("File not found for ID: {}", id);
                    return ResponseEntity.notFound().build();
                }

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                kitchenerService.downloadStream(gridFSFile.getObjectId(), outputStream);
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
}
