package com.littlehorizons.erp.model;

/**
 * Domain entity representing a parent or legal guardian.
 */
public class Guardian {

    private int id;
    private String documentType;
    private String documentNumber;
    private String firstName;
    private String secondName;
    private String lastName;
    private String secondLastName;
    private String relationship;
    private String mainPhone;
    private String secondaryPhone;
    private String email;
    private String address;
    private String occupation;
    private boolean isFinancialResponsible;

    public Guardian() {
    }

    public Guardian(int id, String documentType, String documentNumber, String firstName, String secondName,
                    String lastName, String secondLastName, String relationship, String mainPhone,
                    String secondaryPhone, String email, String address, String occupation,
                    boolean isFinancialResponsible) {
        this.id = id;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.firstName = firstName;
        this.secondName = secondName;
        this.lastName = lastName;
        this.secondLastName = secondLastName;
        this.relationship = relationship;
        this.mainPhone = mainPhone;
        this.secondaryPhone = secondaryPhone;
        this.email = email;
        this.address = address;
        this.occupation = occupation;
        this.isFinancialResponsible = isFinancialResponsible;
    }

    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        if (firstName != null) sb.append(firstName).append(" ");
        if (secondName != null && !secondName.isBlank()) sb.append(secondName).append(" ");
        if (lastName != null) sb.append(lastName).append(" ");
        if (secondLastName != null && !secondLastName.isBlank()) sb.append(secondLastName);
        return sb.toString().trim();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getSecondName() { return secondName; }
    public void setSecondName(String secondName) { this.secondName = secondName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getSecondLastName() { return secondLastName; }
    public void setSecondLastName(String secondLastName) { this.secondLastName = secondLastName; }

    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }

    public String getMainPhone() { return mainPhone; }
    public void setMainPhone(String mainPhone) { this.mainPhone = mainPhone; }

    public String getSecondaryPhone() { return secondaryPhone; }
    public void setSecondaryPhone(String secondaryPhone) { this.secondaryPhone = secondaryPhone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }

    public boolean isFinancialResponsible() { return isFinancialResponsible; }
    public void setFinancialResponsible(boolean financialResponsible) { isFinancialResponsible = financialResponsible; }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s: %s) - Parentesco: %s - Tel: %s",
                id, getFullName(), documentType, documentNumber, relationship, mainPhone);
    }
}
