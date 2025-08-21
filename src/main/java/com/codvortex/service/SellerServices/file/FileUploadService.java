package com.codvortex.service.SellerServices.file;


import com.codvortex.configuration.JwtTokenService;
import com.codvortex.domain.File;
import com.codvortex.domain.Product;
import com.codvortex.domain.User;
import com.codvortex.repository.FileRepository;
import com.codvortex.repository.ProductRepository;
import com.codvortex.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@Transactional
public class FileUploadService {

    @Autowired
    private FileRepository fileDataRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtTokenService jwtTokenService;
    @Autowired
    private ProductRepository productRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    public File storeFile(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        long fileSize = file.getSize();

        File existingFile = fileDataRepository.findByNameAndSize(fileName, fileSize);
        if (existingFile != null) {
            return existingFile;
        }

        File fileData = new File();
        fileData.setName(fileName);
        fileData.setType(file.getContentType());
        fileData.setSize(fileSize);
        fileData.setData(file.getBytes());

        return fileDataRepository.save(fileData);
    }

    public String storeProductImage(Long id, MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        long fileSize = file.getSize();

        File existingFile = fileDataRepository.findByNameAndSize(fileName, fileSize);
        File savedFile;

        if (existingFile != null) {
            savedFile = existingFile;
        } else {
            File fileData = new File();
            fileData.setName(fileName);
            fileData.setType(file.getContentType());
            fileData.setSize(fileSize);
            fileData.setData(file.getBytes());

            savedFile = fileDataRepository.save(fileData);
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        String fileUrl = baseUrl + "/files/download/" + savedFile.getId();
        product.setImg(fileUrl);
        productRepository.save(product);

        return fileUrl;
    }


    public File storeIdentity(MultipartFile file, String token) throws IOException {
        User user = userRepository.findByUsername(jwtTokenService.extractEmail(token))
                .orElseThrow(() -> new RuntimeException("User not found"));

        String fileName = file.getOriginalFilename();
        long fileSize = file.getSize();

        File fileData = new File();
        fileData.setName(fileName);
        fileData.setType(file.getContentType());
        fileData.setSize(fileSize);
        fileData.setData(file.getBytes());
        fileData.setUser(user);

        user.setActive(true);
        return fileDataRepository.save(fileData);
    }

    public File getFile(Long id) {
        return fileDataRepository.findById(id).orElse(null);
    }
}