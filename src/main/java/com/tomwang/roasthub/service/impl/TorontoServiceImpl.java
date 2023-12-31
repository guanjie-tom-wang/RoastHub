package com.tomwang.roasthub.service.impl;


import com.tomwang.roasthub.service.TorontoService;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Service
public class TorontoServiceImpl implements TorontoService {
    private static final Logger logger = LoggerFactory.getLogger(TorontoServiceImpl.class);
    private final Random random = new Random();
    @Autowired
    private MongoTemplate mongoTemplate;
    private GridFSBucket gridFSBucket;

    @PostConstruct
    private void init() {
        gridFSBucket = GridFSBuckets.create(mongoTemplate.getDb(), "dinner");
    }

    public List<String> storeFiles(MultipartFile[] files) throws IOException {
        List<String> fileIds = new ArrayList<>();
        String itemName = generateItemName(Objects.requireNonNull(files[0].getOriginalFilename()));
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashInBytes = md.digest(itemName.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hashInBytes) {
                sb.append(String.format("%02x", b));
            }
            itemName = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 hashing algorithm not found", e);
        }
        for (MultipartFile file : files) {
            // 生成或获取 itemName
            try (InputStream inputStream = file.getInputStream()) {
                Document metadata = new Document("type", "file")
                        .append("content_type", file.getContentType())
                        .append("file_type", getFileType(file.getOriginalFilename()))
                        .append("itemName", itemName); // 添加 itemName 到元数据

                GridFSUploadOptions options = new GridFSUploadOptions()
                        .chunkSizeBytes(1024)
                        .metadata(metadata);

                ObjectId fileId = gridFSBucket.uploadFromStream(file.getOriginalFilename(), inputStream, options);
                fileIds.add(fileId.toHexString());
            }
        }
        return fileIds;
    }

    @Override
    public List<String> storeFiles(MultipartFile image, MultipartFile file, String name, String type, String userName) throws IOException {
        List<String> fileIds = new ArrayList<>();
        String reciptName = name;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashInBytes = md.digest(name.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hashInBytes) {
                sb.append(String.format("%02x", b));
            }
            name = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 hashing algorithm not found", e);
        }
        try (InputStream inputStream = file.getInputStream()) {
            Document metadata = new Document("type", "file")
                    .append("content_type", file.getContentType())
                    .append("file_type", getFileType(file.getOriginalFilename()))
                    .append("itemName", name)
                    .append("name", reciptName)
                    .append("userName", userName)
                    .append("type", type);

            GridFSUploadOptions options = new GridFSUploadOptions()
                    .chunkSizeBytes(1024)
                    .metadata(metadata);

            ObjectId fileId = gridFSBucket.uploadFromStream(file.getOriginalFilename(), inputStream, options);
            fileIds.add(fileId.toHexString());
        }
        try (InputStream inputStream = image.getInputStream()) {
            Document metadata = new Document("type", "file")
                    .append("content_type", image.getContentType())
                    .append("file_type", getFileType(image.getOriginalFilename()))
                    .append("itemName", name)
                    .append("name", reciptName)
                    .append("userName", userName)
                    .append("type", type);


            GridFSUploadOptions options = new GridFSUploadOptions()
                    .chunkSizeBytes(1024)
                    .metadata(metadata);

            ObjectId fileId = gridFSBucket.uploadFromStream(image.getOriginalFilename(), inputStream, options);
            fileIds.add(fileId.toHexString());
        }
        return fileIds;
    }


    @Override
    public List<GridFSFile> listFiles() {
        List<GridFSFile> files = new ArrayList<>();
        gridFSBucket.find().into(files);
        return files;
    }
    @Override
    public void delete(ObjectId id) {
        gridFSBucket.delete((ObjectId) id);
    }

    private String getFileType(String fileName) {
        if (fileName.endsWith(".pdf")) {
            return "pdf";
        } else if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image";
        } else {
            return "unknown";
        }
    }

    @Override
    public GridFSFile getFile(ObjectId id) {
        GridFSFile gridFSFile = gridFSBucket.find(Filters.eq("_id", id)).first();
        return gridFSFile;
    }

    @Override
    public void downloadStream(ObjectId objectId, ByteArrayOutputStream outputStream) {
        gridFSBucket.downloadToStream(objectId, outputStream);
    }

    @Override
    public List<GridFSFile> getRandomFiles(int count) {
        Bson imageFilter = Filters.eq("metadata.file_type", "image");
        List<GridFSFile> allFiles = gridFSBucket.find(imageFilter).into(new ArrayList<>());
        List<GridFSFile> randomFiles = new ArrayList<>();
        logger.info("allFiles" + allFiles.size());

        for (int i = 0; i < count; i++) {
            if (allFiles.isEmpty()) {
                break;
            }
            int randomIndex = random.nextInt(allFiles.size());
            randomFiles.add(allFiles.remove(randomIndex));
        }

        return randomFiles;
    }

    public ObjectId findImageIdByName(String name) {
        Bson query = Filters.and(
                Filters.eq("metadata.itemName", name),
                Filters.eq("metadata.file_type", "image")
        );

        GridFSFindIterable files = gridFSBucket.find(query);

        GridFSFile file = files.first();
        if (file != null) {
            return file.getObjectId();
        }
        return null;
    }

    @Override
    public ObjectId findPdfIdByName(String name) {
        Bson query = Filters.and(
                Filters.eq("metadata.itemName", name),
                Filters.eq("metadata.file_type", "pdf")
        );

        GridFSFindIterable files = gridFSBucket.find(query);

        GridFSFile file = files.first();
        if (file != null) {
            return file.getObjectId();
        }
        return null;
    }

    @Override
    public String generateFileUrl(ObjectId fileId, String type) {
        if (fileId == null) {
            throw new IllegalArgumentException("FileId cannot be null");
        }
        logger.info(fileId.toString());
        return UriComponentsBuilder.fromUriString("/test")
                .pathSegment("lunch", "files", "download", fileId.toHexString())
                .queryParam("type", type) // Optional
                .toUriString();
    }

    private String generateItemName(String originalFilename) {
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            return originalFilename.substring(0, dotIndex);
        }
        return originalFilename;
    }
}