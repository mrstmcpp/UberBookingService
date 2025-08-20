package org.mrstm.uberbookingservice.dto;


import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long bookingId;
    private String bookingStatus;
    private Long driverId;
    private String fullName;
}
