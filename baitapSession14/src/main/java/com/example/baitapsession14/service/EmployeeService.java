package com.example.baitapsession14.service;

import com.cloudinary.Cloudinary;
import com.example.baitapsession14.dto.EmployeeCreateDTO;
import com.example.baitapsession14.model.Employee;
import com.example.baitapsession14.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final Cloudinary cloudinary;

    @Transactional(readOnly = true)
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    @Transactional
    public Employee create(EmployeeCreateDTO dto) {
        var file = dto.getAvatarFile();
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vui lòng chọn ảnh đại diện");
        }
        if (file.getContentType() == null || !Set.of("image/jpeg", "image/png", "image/webp").contains(file.getContentType())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ảnh phải có định dạng JPG, PNG hoặc WebP");
        }
        if (!StringUtils.hasText(cloudinary.config.cloudName) || !StringUtils.hasText(cloudinary.config.apiKey)
                || !StringUtils.hasText(cloudinary.config.apiSecret)) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Chưa cấu hình tài khoản Cloudinary");
        }

        Map<?, ?> result;
        try {
            result = cloudinary.uploader().upload(file.getBytes(),
                    Map.of("folder", "session14/employees", "resource_type", "image"));
        } catch (IOException | RuntimeException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Không thể tải ảnh lên Cloudinary");
        }
        Object secureUrl = result.get("secure_url");
        if (!(secureUrl instanceof String url) || !url.startsWith("https://")) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Cloudinary không trả về đường dẫn ảnh hợp lệ");
        }

        Employee employee = new Employee();
        employee.setFullName(dto.getFullName());
        employee.setEmail(dto.getEmail());
        employee.setDepartment(dto.getDepartment());
        employee.setAvatarUrl(url);
        return employeeRepository.save(employee);
    }
}
