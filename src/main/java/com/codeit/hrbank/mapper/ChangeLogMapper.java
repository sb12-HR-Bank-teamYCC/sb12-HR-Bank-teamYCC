package com.codeit.hrbank.mapper;

import com.codeit.hrbank.dto.changeLog.ChangeLogDetailDto;
import com.codeit.hrbank.dto.changeLog.ChangeLogDto;
import com.codeit.hrbank.dto.changeLog.DiffDto;
import com.codeit.hrbank.entity.changeLog.ChangeLog;
import com.codeit.hrbank.entity.changeLog.DetailChangeLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ChangeLogMapper {

    ChangeLogDto toDto(ChangeLog changeLog);

    @Mapping(target = "diffs", source = "diffs")
    ChangeLogDetailDto toDetailDto(ChangeLog changeLog);

    @Mapping(target = "before", source = "beforeValue")
    @Mapping(target = "after", source = "afterValue")
    DiffDto toDiffDto(DetailChangeLog diff);
}
