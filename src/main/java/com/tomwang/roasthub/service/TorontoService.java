package com.tomwang.roasthub.service;

import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public interface TorontoService {
    List<String> storeFiles(MultipartFile[] files) throws IOException;

    List<String> storeFiles(MultipartFile image, MultipartFile file, String name, String type, String userName) throws IOException;


    List<GridFSFile> listFiles();

    GridFSFile getFile(ObjectId id);

    void downloadStream(ObjectId objectId, ByteArrayOutputStream outputStream);

    List<GridFSFile> getRandomFiles(int count);

    ObjectId findImageIdByName(String name);

    ObjectId findPdfIdByName(String name);

    String generateFileUrl(ObjectId fileId, String type);
    void delete(ObjectId id);

}
