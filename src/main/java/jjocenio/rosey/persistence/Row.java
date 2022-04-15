package jjocenio.rosey.persistence;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jjocenio.rosey.persistence.converter.JsonConverter;

import javax.persistence.*;
import java.util.Date;
import java.util.Map;

@Entity
@Table(name = "row")
public class Row {

    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    @Lob
    @Column(length = 100000)
    private String resultDetail;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Date lastUpdate = new Date();

    @Lob
    @Column(length = 100000)
    @Convert(converter = JsonConverter.class)
    private Map<String, Object> data;

    private String output;

    public Row() {
    }

    public Row(Long id) {
        this.id = id;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getResultDetail() {
        return resultDetail;
    }

    public void setResultDetail(String resultDetail) {
        this.resultDetail = resultDetail;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    @Override
    public String toString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "Error converting to string: " + e.getMessage();
        }
    }

    public enum Status {
        PENDING, PROCESSING, PROCESSED, FAILED;
    }
}
