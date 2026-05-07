package com.AgsCh.task_scheduler.model;

import jakarta.persistence.*;

@Entity
@Table(name = "word_templates")
public class WordTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    private String contentType;

    @Lob
    @Column(nullable = false)
    private byte[] data;

    @OneToOne
    @JoinColumn(name = "house_id", nullable = false, unique = true)
    private House house;

    protected WordTemplate() {
    }

    public WordTemplate(String fileName,
            String contentType,
            byte[] data,
            House house) {

        this.fileName = fileName;
        this.contentType = contentType;
        this.data = data;
        this.house = house;
    }

    public byte[] getData() {
        return data;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setHouse(House house) {
        this.house = house;
    }
}