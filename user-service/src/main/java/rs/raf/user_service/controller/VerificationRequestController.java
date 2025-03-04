package rs.raf.user_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.raf.user_service.bankClient.BankClient;
import rs.raf.user_service.dto.ClientDto;
import rs.raf.user_service.dto.VerificationRequestDto;
import rs.raf.user_service.entity.VerificationRequest;
import rs.raf.user_service.enums.VerificationStatus;
import rs.raf.user_service.exceptions.VerificationClientNotFoundException;
import rs.raf.user_service.service.ClientService;
import rs.raf.user_service.service.VerificationRequestService;

import java.util.List;

@RestController
@RequestMapping("/api/verification")
public class VerificationRequestController {

    private final VerificationRequestService verificationRequestService;
    private final ClientService clientService;


    private final BankClient bankClient;

    public VerificationRequestController(VerificationRequestService verificationRequestService, ClientService clientService, BankClient bankClient) {
        this.verificationRequestService = verificationRequestService;
        this.clientService = clientService;

        this.bankClient = bankClient;
    }


    @Operation(summary = "Get active verification requests", description = "Returns a list of pending verification requests for the user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of active requests retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @GetMapping("/active-requests")
    public ResponseEntity<List<VerificationRequest>> getActiveRequests() {

        try {
            ClientDto client = clientService.getCurrentClient();

            List<VerificationRequest> requests = verificationRequestService.getActiveRequests(client.getId());
            return ResponseEntity.ok(requests);

        } catch (Exception e) {
            throw new VerificationClientNotFoundException();
        }

    }

//    @Operation(summary = "Approve verification request", description = "Approves a verification request for a specific transaction.")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Verification request approved"),
//            @ApiResponse(responseCode = "400", description = "Request not found or already processed")
//    })
//    @PostMapping("/approve/{requestId}")
//    public ResponseEntity<String> approveRequest(@PathVariable Long requestId) {
//        boolean updated = verificationRequestService.updateRequestStatus(requestId, VerificationStatus.APPROVED);
//        return updated ? ResponseEntity.ok("Request approved") : ResponseEntity.badRequest().body("Request not found or already processed");
//    }

    @Operation(summary = "Deny verification request", description = "Denies a verification request for a specific transaction.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verification request denied"),
            @ApiResponse(responseCode = "400", description = "Request not found or already processed")
    })
    @PostMapping("/deny/{requestId}")
    public ResponseEntity<String> denyRequest(@PathVariable Long requestId) {
        boolean updated = verificationRequestService.updateRequestStatus(requestId, VerificationStatus.DENIED);
        return updated ? ResponseEntity.ok("Request denied") : ResponseEntity.badRequest().body("Request not found or already processed");
    }

//    @PostMapping("/check-verification")
//    public boolean isVerificationApproved(@RequestParam Long targetId, @RequestParam String verificationCode) {
//        return verificationRequestService.isVerificationApproved(targetId, verificationCode);
//    }

    @PostMapping("/request")
    public ResponseEntity<String> createVerificationRequest(@RequestBody VerificationRequestDto request) {
        verificationRequestService.createVerificationRequest(
                request.getUserId(),
                request.getEmail(),
                request.getCode(),
                request.getTargetId()
        );
        return ResponseEntity.ok("Verification request created.");
    }

    @GetMapping("/status/{targetId}")
    public ResponseEntity<Boolean> isVerificationApproved(
            @PathVariable Long targetId,
            @RequestParam("code") String verificationCode) {

        boolean isApproved = verificationRequestService.isVerificationApproved(targetId, verificationCode);
        return ResponseEntity.ok(isApproved);
    }


    @Operation(summary = "Approve verification request", description = "Approves a verification request for a specific transaction.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verification request approved"),
            @ApiResponse(responseCode = "400", description = "Request not found or already processed")
    })
    @PostMapping("/approve/request/{requestId}")
    public ResponseEntity<String> approveRequest(@PathVariable Long requestId) {
        boolean updated = verificationRequestService.updateRequestStatus(requestId, VerificationStatus.APPROVED);

        if (updated) {
            // Dohvatamo zahtev da bismo izvukli accountId (targetId)
            VerificationRequest request = verificationRequestService.getRequestById(requestId);

            // Pozivamo bank-service da promeni limit
            bankClient.changeAccountLimit(request.getTargetId());

            return ResponseEntity.ok("Request approved and account limit updated");
        }
        return ResponseEntity.badRequest().body("Request not found or already processed");
    }
}
