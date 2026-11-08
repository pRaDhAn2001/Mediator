
package com.mediator.master.service;

import com.mediator.master.entity.Board;
import com.mediator.master.entity.ClassLevel;
import com.mediator.master.entity.Subject;
import com.mediator.master.repository.BoardRepository;
import com.mediator.master.repository.ClassLevelRepository;
import com.mediator.master.repository.SubjectRepository;
import com.mediator.master.dto.BoardResponse;
import com.mediator.master.dto.ClassLevelResponse;
import com.mediator.master.dto.SubjectResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MasterServiceTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private ClassLevelRepository classLevelRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @InjectMocks
    private MasterService masterService;

    @Test
    void getAllBoards_shouldMapAllBoards() {
        Board cbse = mock(Board.class);
        when(cbse.getId()).thenReturn(1L);
        when(cbse.getName()).thenReturn("CBSE");

        Board icse = mock(Board.class);
        when(icse.getId()).thenReturn(2L);
        when(icse.getName()).thenReturn("ICSE");

        when(boardRepository.findAll()).thenReturn(List.of(cbse, icse));

        List<BoardResponse> result = masterService.getAllBoards();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("CBSE", result.get(0).getName());
        assertEquals(2L, result.get(1).getId());
        assertEquals("ICSE", result.get(1).getName());

        verify(boardRepository).findAll();
        verifyNoInteractions(classLevelRepository, subjectRepository);
    }

    @Test
    void getAllBoards_shouldReturnEmptyListWhenNoBoardsExist() {
        when(boardRepository.findAll()).thenReturn(List.of());

        assertTrue(masterService.getAllBoards().isEmpty());

        verify(boardRepository).findAll();
    }

    @Test
    void getAllClassLevels_shouldMapAllClassLevels() {
        ClassLevel class8 = mock(ClassLevel.class);
        when(class8.getId()).thenReturn(8L);
        when(class8.getStandard()).thenReturn(8);

        ClassLevel class10 = mock(ClassLevel.class);
        when(class10.getId()).thenReturn(10L);
        when(class10.getStandard()).thenReturn(10);

        when(classLevelRepository.findAll()).thenReturn(List.of(class8, class10));

        List<ClassLevelResponse> result = masterService.getAllClassLevels();

        assertEquals(2, result.size());
        assertEquals(8L, result.get(0).getId());
        assertEquals(8, result.get(0).getStandard());
        assertEquals(10L, result.get(1).getId());
        assertEquals(10, result.get(1).getStandard());

        verify(classLevelRepository).findAll();
        verifyNoInteractions(boardRepository, subjectRepository);
    }

    @Test
    void getAllClassLevels_shouldReturnEmptyListWhenNoneExist() {
        when(classLevelRepository.findAll()).thenReturn(List.of());

        assertTrue(masterService.getAllClassLevels().isEmpty());

        verify(classLevelRepository).findAll();
    }

    @Test
    void getAllSubjects_shouldMapAllSubjects() {
        Subject mathematics = mock(Subject.class);
        when(mathematics.getId()).thenReturn(1L);
        when(mathematics.getName()).thenReturn("Mathematics");

        Subject physics = mock(Subject.class);
        when(physics.getId()).thenReturn(2L);
        when(physics.getName()).thenReturn("Physics");

        when(subjectRepository.findAll()).thenReturn(List.of(mathematics, physics));

        List<SubjectResponse> result = masterService.getAllSubjects();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("Mathematics", result.get(0).getName());
        assertEquals(2L, result.get(1).getId());
        assertEquals("Physics", result.get(1).getName());

        verify(subjectRepository).findAll();
        verifyNoInteractions(boardRepository, classLevelRepository);
    }

    @Test
    void getAllSubjects_shouldReturnEmptyListWhenNoneExist() {
        when(subjectRepository.findAll()).thenReturn(List.of());

        assertTrue(masterService.getAllSubjects().isEmpty());

        verify(subjectRepository).findAll();
    }
}
