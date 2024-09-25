import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;

public class SnsFCMPublisher {

    private final SnsClient snsClient;

    public SnsFCMPublisher(SnsClient snsClient) {
        this.snsClient = snsClient;
    }

    // Publish a Firebase notification message via SNS
    public void publishFirebaseMessage(String platformEndpointArn, String title, String body) {
        String firebaseMessage = "{ \"GCM\": \"{ " +
                "\\\"notification\\\": { \\\"title\\\": \\\"" + title + "\\\", \\\"body\\\": \\\"" + body + "\\\" }, " +
                "\\\"data\\\": { \\\"key1\\\": \\\"value1\\\", \\\"key2\\\": \\\"value2\\\" } " +
                "}\" }";

        try {
            PublishRequest publishRequest = PublishRequest.builder()
                    .message(firebaseMessage)
                    .messageStructure("json")
                    .targetArn(platformEndpointArn)
                    .build();

            PublishResponse publishResponse = snsClient.publish(publishRequest);
            System.out.println("MessageId: " + publishResponse.messageId());

        } catch (SnsException e) {
            System.err.println("Failed to send notification: " + e.awsErrorDetails().errorMessage());
        }
    }

    public static void main(String[] args) {
        Region region = Region.US_EAST_1; // Choose your region
        SnsClient snsClient = SnsClient.builder().region(region).build();

        SnsFCMPublisher publisher = new SnsFCMPublisher(snsClient);

        String platformEndpointArn = "arn:aws:sns:us-east-1:123456789012:endpoint/GCM/MyApp/endpointArn";
        String title = "Hello World!";
        String body = "This is a Firebase Cloud Messaging push notification.";

        publisher.publishFirebaseMessage(platformEndpointArn, title, body);
    }
}
