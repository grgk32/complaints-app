package com.example.complaints.mapper;

import com.example.complaints.dto.ComplaintResponse;
import com.example.complaints.model.Complaint;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ComplaintMapper {
    ComplaintResponse toResponse(Complaint complaint);
}
