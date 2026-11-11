package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.TeacherDAO;
import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.model.TeacherAssignment;

import java.util.List;

/**
 * Controller for teacher human resources management and teaching assignments.
 */
public class TeacherController {

    private final TeacherDAO teacherDAO = new TeacherDAO();

    public boolean registerTeacher(Teacher teacher) {
        if (teacher == null || teacher.getDocumentNumber() == null || teacher.getDocumentNumber().isBlank()) {
            return false;
        }
        return teacherDAO.createTeacher(teacher);
    }

    public Teacher findTeacherById(int id) {
        return teacherDAO.findTeacherById(id);
    }

    public Teacher findTeacherByDocument(String documentNumber) {
        return teacherDAO.findTeacherByDocument(documentNumber);
    }

    public List<Teacher> getAllTeachers() {
        return teacherDAO.findAllTeachers();
    }

    public boolean assignSubjectToTeacher(int teacherId, int subjectId, int year, String section) {
        return teacherDAO.assignTeacherToSubject(teacherId, subjectId, year, section);
    }

    public List<TeacherAssignment> getAssignmentsForTeacher(int teacherId, int year) {
        return teacherDAO.getAssignmentsForTeacher(teacherId, year);
    }

    public List<TeacherAssignment> getAllAssignments(int year) {
        return teacherDAO.getAllAssignments(year);
    }
}
