package com.cranesvarsity.template.model.cranescrm;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Maps the existing, live "admission" table in the legacy `cranescrm` schema.
 * Read/lookup only in this phase — never let ddl-auto touch this table.
 */
@Entity
@Table(name = "admission")
public class Admission {

    @Id
    @Column(name = "registration_no")
    private String registrationNo;

    private String email;

    private String password;

    private String stname;

    private String batchno;

    private String dropout;

    private String course;

    @Column(name = "course_domain")
    private String courseDomain;

    private String collegename;

    private String contact;

    private String address;

    @Column(name = "registration_date")
    private LocalDateTime registrationDate;

    public String getRegistrationNo() { return registrationNo; }
    public void setRegistrationNo(String registrationNo) { this.registrationNo = registrationNo; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getStname() { return stname; }
    public void setStname(String stname) { this.stname = stname; }

    public String getBatchno() { return batchno; }
    public void setBatchno(String batchno) { this.batchno = batchno; }

    public String getDropout() { return dropout; }
    public void setDropout(String dropout) { this.dropout = dropout; }

    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }

    public String getCourseDomain() { return courseDomain; }
    public void setCourseDomain(String courseDomain) { this.courseDomain = courseDomain; }

    public String getCollegename() { return collegename; }
    public void setCollegename(String collegename) { this.collegename = collegename; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDateTime getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDateTime registrationDate) { this.registrationDate = registrationDate; }
}
