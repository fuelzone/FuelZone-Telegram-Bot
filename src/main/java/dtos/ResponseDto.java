package dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDto {
    private double latitude;
    private double longitude;
    private String fuelType;
    private double price;
    private double amount;
    private String name;
    private String lastUpdated;
    private String openDate;
    private String closeDate;
    private boolean found;
    private String msg;
    private double distance;
}
