import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreatePlatformEndpointRequest;
import software.amazon.awssdk.services.sns.model.CreatePlatformEndpointResponse;
import software.amazon.awssdk.services.sns.model.ListEndpointsByPlatformApplicationRequest;
import software.amazon.awssdk.services.sns.model.ListEndpointsByPlatformApplicationResponse;
import software.amazon.awssdk.services.sns.model.Endpoint;
import software.amazon.awssdk.services.sns.model.SnsException;

public class SnsPlatformEndpointManager {

    private final SnsClient snsClient;
    private final String platformApplicationArn;

    public SnsPlatformEndpointManager(SnsClient snsClient, String platformApplicationArn) {
        this.snsClient = snsClient;
        this.platformApplicationArn = platformApplicationArn;
    }

    // Create or retrieve an existing platform endpoint
    public String getOrCreatePlatformEndpoint(String deviceToken) {
        String existingEndpointArn = findExistingEndpointArn(deviceToken);

        if (existingEndpointArn != null) {
            return existingEndpointArn;
        }

        return createPlatformEndpoint(deviceToken);
    }

    // Search for an existing endpoint with the same device token
    private String findExistingEndpointArn(String deviceToken) {
        try {
            ListEndpointsByPlatformApplicationRequest request = ListEndpointsByPlatformApplicationRequest.builder()
                    .platformApplicationArn(platformApplicationArn)
                    .build();

            ListEndpointsByPlatformApplicationResponse response = snsClient.listEndpointsByPlatformApplication(request);

            for (Endpoint endpoint : response.endpoints()) {
                if (endpoint.attributes().get("Token").equals(deviceToken)) {
                    return endpoint.endpointArn();
                }
            }
        } catch (SnsException e) {
            System.err.println("Error finding existing endpoint: " + e.awsErrorDetails().errorMessage());
        }
        return null;
    }

    // Create a new platform endpoint
    private String createPlatformEndpoint(String deviceToken) {
        try {
            CreatePlatformEndpointRequest request = CreatePlatformEndpointRequest.builder()
                    .platformApplicationArn(platformApplicationArn)
                    .token(deviceToken)
                    .build();

            CreatePlatformEndpointResponse response = snsClient.createPlatformEndpoint(request);
            return response.endpointArn();
        } catch (SnsException e) {
            System.err.println("Error creating endpoint: " + e.awsErrorDetails().errorMessage());
        }
        return null;
    }

    public static void main(String[] args) {
        Region region = Region.US_EAST_1; // Choose your region
        SnsClient snsClient = SnsClient.builder().region(region).build();
        String platformApplicationArn = "arn:aws:sns:us-east-1:123456789012:app/GCM/MyApp"; // Replace with your platform ARN

        SnsPlatformEndpointManager manager = new SnsPlatformEndpointManager(snsClient, platformApplicationArn);

        String deviceToken = "example_device_token"; // Replace with the actual device token
        String endpointArn = manager.getOrCreatePlatformEndpoint(deviceToken);

        if (endpointArn != null) {
            System.out.println("Platform Endpoint ARN: " + endpointArn);
        } else {
            System.out.println("Failed to create or retrieve platform endpoint.");
        }
    }
}
