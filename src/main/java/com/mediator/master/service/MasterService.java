package com.mediator.master.service;

import com.mediator.master.dto.BoardResponse;
import com.mediator.master.dto.ClassLevelResponse;
import com.mediator.master.dto.SubjectResponse;
import com.mediator.master.repository.BoardRepository;
import com.mediator.master.repository.ClassLevelRepository;
import com.mediator.master.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MasterService {

    private final BoardRepository boardRepository;
    private final ClassLevelRepository classLevelRepository;
    private final SubjectRepository subjectRepository;

    @Transactional(readOnly = true)
    public List<BoardResponse> getAllBoards() {
        return boardRepository.findAll().stream()
                .map(b -> BoardResponse.builder().id(b.getId()).name(b.getName()).build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClassLevelResponse> getAllClassLevels() {
        return classLevelRepository.findAll().stream()
                .map(c -> ClassLevelResponse.builder().id(c.getId()).standard(c.getStandard()).build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> getAllSubjects() {
        return subjectRepository.findAll().stream()
                .map(s -> SubjectResponse.builder().id(s.getId()).name(s.getName()).build())
                .toList();
    }
}
