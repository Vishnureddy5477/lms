package com.cranesvarsity.template.model.cranescrm;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Maps the existing "manage_payment" table in `cranescrm`. Insert-only in
 * this phase (student submits payment details for the accounts team to
 * verify) — never let ddl-auto touch this table.
 */
@Entity
@Table(name = "manage_payment")
public class ManagePayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String stname;

    private String stemail;

    private String regno;

    private String contact;

    private String batch;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "payment_details")
    private String paymentDetails;

    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "action_token")
    private String actionToken;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStname() { return stname; }
    public void setStname(String stname) { this.stname = stname; }

    public String getStemail() { return stemail; }
    public void setStemail(String stemail) { this.stemail = stemail; }

    public String getRegno() { return regno; }
    public void setRegno(String regno) { this.regno = regno; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getBatch() { return batch; }
    public void setBatch(String batch) { this.batch = batch; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentDetails() { return paymentDetails; }
    public void setPaymentDetails(String paymentDetails) { this.paymentDetails = paymentDetails; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getActionToken() { return actionToken; }
    public void setActionToken(String actionToken) { this.actionToken = actionToken; }
}
