package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.GuardianDAO;
import com.littlehorizons.erp.dao.StudentDAO;
import com.littlehorizons.erp.model.Enrollment;
import com.littlehorizons.erp.model.GradeLevel;
import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.Student;

import java.util.List;

/**
 * Controller managing student admissions, guardian associations, and grade enrollments.
 */
public class StudentController {

    private final StudentDAO studentDAO = new StudentDAO();
    private final GuardianDAO guardianDAO = new GuardianDAO();

    public boolean registerStudent(Student student) {
        if (student == null || student.getDocumentNumber() == null || student.getDocumentNumber().isBlank()) {
            return false;
        }
        return studentDAO.createStudent(student);
    }

    public boolean registerGuardian(Guardian guardian) {
        if (guardian == null || guardian.getDocumentNumber() == null || guardian.getDocumentNumber().isBlank()) {
            return false;
        }
        return guardianDAO.createGuardian(guardian);
    }

    public boolean linkGuardianToStudent(int studentId, int guardianId, boolean isPrimary, boolean isEmergency) {
        return studentDAO.linkGuardianToStudent(studentId, guardianId, isPrimary, isEmergency);
    }

    public boolean enrollStudentInGrade(int studentId, int gradeId, int year, String remarks) {
        return studentDAO.enrollStudentInGrade(studentId, gradeId, year, remarks);
    }

    public Student findStudentById(int id) {
        return studentDAO.findStudentById(id);
    }

    public Student findStudentByDocument(String documentNumber) {
        return studentDAO.findStudentByDocument(documentNumber);
    }

    public List<Student> getAllStudents() {
        return studentDAO.findAllStudents();
    }

    public List<Guardian> getAllGuardians() {
        return guardianDAO.findAllGuardians();
    }

    public List<Guardian> getGuardiansForStudent(int studentId) {
        return studentDAO.getGuardiansForStudent(studentId);
    }

    public List<Student> getStudentsForGuardian(int guardianId) {
        return studentDAO.getStudentsForGuardian(guardianId);
    }

    public List<Enrollment> getStudentEnrollments(int studentId) {
        return studentDAO.getEnrollmentsForStudent(studentId);
    }

    public List<GradeLevel> getAllGradeLevels() {
        return studentDAO.findAllGradeLevels();
    }
}
