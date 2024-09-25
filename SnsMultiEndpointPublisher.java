import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;
import java.util.List;

public class SnsMultiEndpointPublisher {

    private final SnsClient snsClient;

    public SnsMultiEndpointPublisher(SnsClient snsClient) {
        this.snsClient = snsClient;
    }

    // Publish a message to multiple endpoints
    public void publishToMultipleEndpoints(List<String> endpointArns, String message) {
        for (String endpointArn : endpointArns) {
            try {
                PublishRequest request = PublishRequest.builder()
                        .message(message)
                        .targetArn(endpointArn)
                        .build();

                PublishResponse response = snsClient.publish(request);
                System.out.println("MessageId for " + endpointArn + ": " + response.messageId());
            } catch (SnsException e) {
                System.err.println("Failed to publish to " + endpointArn + ": " + e.awsErrorDetails().errorMessage());
            }
        }
    }

    public static void main(String[] args) {
        Region region = Region.US_EAST_1; // Choose your region
        SnsClient snsClient = SnsClient.builder().region(region).build();

        SnsMultiEndpointPublisher publisher = new SnsMultiEndpointPublisher(snsClient);

        // Replace with your actual platform endpoint ARNs
        List<String> endpointArns = List.of(
                "arn:aws:sns:us-east-1:123456789012:endpoint/GCM/MyApp/endpoint1",
                "arn:aws:sns:us-east-1:123456789012:endpoint/GCM/MyApp/endpoint2",
                "arn:aws:sns:us-east-1:123456789012:endpoint/GCM/MyApp/endpoint3"
        );
        String message = "Hello, this is a broadcast message!";

        publisher.publishToMultipleEndpoints(endpointArns, message);
    }
}
