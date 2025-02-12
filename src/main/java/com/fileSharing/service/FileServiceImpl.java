package com.fileSharing.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.fileSharing.entity.FileEntity;
import com.fileSharing.exception.FileNotFoundException;
import com.fileSharing.model.FileModel;
import com.fileSharing.repository.FileRepository;

@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private FileRepository fileRepository;

    private FileModel convertToModel(FileEntity entity) {
        FileModel model = new FileModel();
        BeanUtils.copyProperties(entity, model);
        return model;
    }

    @Override
    public List<FileModel> getAll() {
        List<FileEntity> entities = fileRepository.findAll();
        return entities.stream().map(this::convertToModel).collect(Collectors.toList());
    }

    @Override
    public ResponseEntity<?> uploadFile(MultipartFile file, String uploadedBy) throws IOException {
        Path path = Paths.get("uploads");
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }

        FileEntity entity = new FileEntity();
        entity.setFileName(file.getOriginalFilename());
        entity.setUploadedBy(uploadedBy);
        entity.setExpiryTime(LocalDateTime.now().plusDays(1));
        entity.setUploadTime(LocalDateTime.now());
        fileRepository.save(entity);

        // Save the file to the uploads directory
        Path filePath = path.resolve(file.getOriginalFilename());
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return ResponseEntity.ok().body(convertToModel(entity));
    }

    @Override
    public ResponseEntity<?> shareFile(int id) {
        Optional<FileEntity> entity = fileRepository.findById(id);
        if (entity.isPresent()) {
            return ResponseEntity.ok().body(convertToModel(entity.get()));
        } else {
            throw new FileNotFoundException("File Not Found");
        }
    }

    @Override
    public ResponseEntity<?> deleteFile(int id) {
        Optional<FileEntity> entity = fileRepository.findById(id);
        if (entity.isPresent()) {
            fileRepository.delete(entity.get());
            return ResponseEntity.ok().body("File Deleted Successfully");
        } else {
            throw new FileNotFoundException("File not Found");
        }
    }

    @Override
    public ResponseEntity<?> getFile(int id) {
        Optional<FileEntity> entity = fileRepository.findById(id);
        if (entity.isPresent()) {
            FileEntity fileEntity = entity.get();
            String fileName = fileEntity.getFileName();
            Path path = Paths.get("uploads/" + fileName);
            byte[] fileContent;

            try {
                fileContent = Files.readAllBytes(path);
            } catch (IOException e) {
                throw new FileNotFoundException("File Not Found");
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(fileContent);
        } else {
            throw new FileNotFoundException("File Not Found");
        }
    }

    @Override
    @Scheduled(cron = "0 0 * * * *")
    public void deleteExpiredFiles() {

        List<FileEntity> entities = fileRepository.findByExpiryTimeBefore(LocalDateTime.now());
        entities.forEach(fileRepository::delete);
        System.out.println("deleted successful" + LocalDateTime.now());
    }

}
