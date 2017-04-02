package awss3plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;

/**
 * @author Zachary Wentworth
 */
public class AWSManager {

	private String m_accessKey;
	private String m_secretKey;
	private static AmazonS3 m_conn;

	private String m_userUploadBucket;

	/**
	 * getter to get the Amazon S3 connection data member
	 * 
	 * @returns m_conn
	 */
	public AmazonS3 get_m_conn() {
		return m_conn;
	}

	/**
	 * getter returns the bucket being uploaded to
	 * 
	 * @returns String m_userUploadBucket
	 */
	public String get_userUploadBucket() {
		return m_userUploadBucket;
	}

	/**
	 * setter sets the bucket being uploaded to
	 * 
	 * @param m_userUploadBucket
	 *            the bucket to upload to
	 */
	public void set_userUploadBucket(String m_userUploadBucket) {
		this.m_userUploadBucket = m_userUploadBucket;
	}

	/**
	 * getter returns the accessKey
	 * 
	 * @returns String m_accessKey
	 */
	public String get_accessKey() {
		return m_accessKey;
	}

	/**
	 * setter sets the accessKey to be used to access AWS
	 * 
	 * @param m_accessKey
	 *            the access key to be used
	 */
	public void set_accessKey(String m_accessKey) {
		this.m_accessKey = m_accessKey;
	}

	/**
	 * getter returns the secretKey
	 * 
	 * @returns String m_secretKey
	 */
	public String get_secretKey() {
		return m_secretKey;
	}

	/**
	 * setter sets the secretKey to be used to access AWS
	 */
	public void set_secretKey(String m_secretKey) {
		this.m_secretKey = m_secretKey;
	}

	/**
	 * Default Constructor: Sets data members to null
	 */
	public AWSManager() {
		m_accessKey = "";
		m_secretKey = "";
		m_userUploadBucket = "";
	}

	/**
	 * 2 arg Constructor used to set the credentials for use
	 *
	 * @param accessKey
	 *            String for AWS accessKey
	 * @param secretKey
	 *            String for AWS secretKey
	 * @return none
	 */
	public AWSManager(String accessKey, String secretKey) {
		m_accessKey = accessKey;
		m_secretKey = secretKey;
	}

	/**
	 * Copy Constructor
	 *
	 * @param rhs
	 *            the AWSManager to be copied
	 * @return none
	 */
	public AWSManager(AWSManager rhs) {
		System.out.println("Copy constructor called");
		this.m_accessKey = rhs.m_accessKey;
		this.m_secretKey = rhs.m_secretKey;
		AWSManager.m_conn = rhs.m_conn;
		this.m_userUploadBucket = rhs.m_userUploadBucket;
	}

	/**
	 * function used for logging into the AWS S3 Server using the provided
	 * credentials.
	 */
	public void Authenticate() {

		AWSCredentials credentials = new BasicAWSCredentials(m_accessKey, m_secretKey);

		ClientConfiguration clientConfig = new ClientConfiguration();
		clientConfig.setProtocol(Protocol.HTTP);

		m_conn = new AmazonS3Client(credentials, clientConfig);
		testAuthentication();
	}

	/**
	 * function checks if the AWS credentials are valid
	 */
	private void testAuthentication() {
		// throws a 403 invalid error if it can't access the list of buckets
		m_conn.listBuckets();
	}

	/**
	 * function gets the list of buckets on the s3 server, and returns the name
	 * of these buckets
	 */
	public ArrayList<String> getS3Buckets() {
		List<Bucket> buckets = m_conn.listBuckets();
		ArrayList<String> bucketNames = new ArrayList<String>();

		for (Bucket bucket : buckets) {
			System.out.println(bucket.getName());
			// check if the bucket is accessible before adding it to the list
			bucketNames.add(bucket.getName());
		}

		return bucketNames;
	}

	/**
	 * function used for listing items within an S3 bucket
	 *
	 * @param bucketName
	 *            String value whether or no the authentication was successful
	 */
	public void getItemsInBucket(String bucketName) {
		Bucket bucket = new Bucket(bucketName);

		try {

			ObjectListing objects = m_conn.listObjects(bucket.getName());
			do {
				for (S3ObjectSummary objectSummary : objects.getObjectSummaries()) {
					System.out.println(objectSummary.getKey());
				}
				objects = m_conn.listNextBatchOfObjects(objects);
			} while (objects.isTruncated());

		} catch (AmazonS3Exception e) {
			System.out.println(e.toString());
		}

	}

	/**
	 * used for uploading an item to the s3 server
	 *
	 * @param bucketName
	 *            bucket to upload the file to
	 * @param filePath
	 *            path of the file that is to be uploaded
	 * @para ID ID name to set for the upload file
	 */
	public void uploadToS3(String bucketName, File file, String ID) {

		PutObjectRequest myPutObject = new PutObjectRequest(bucketName, ID, file);

		ObjectMetadata metaData = new ObjectMetadata();

		metaData.setContentType("application/octet-stream");
		myPutObject.setMetadata(metaData);
		m_conn.putObject(myPutObject);

	}

	/**
	 * method used to check if the user has access to the bucket
	 *
	 * @param bucketName
	 *            The bucket which AWS S3 is trying to confirm access to
	 *
	 */
	public boolean checkIfAccessible(String bucketName) {
		boolean accessible = false;

		// file to be uploaded and deleted from the S3 bucket
		// because the user must be able to upload and delete the workspace from
		// S3
		File bucketTestFile = new File("authTests\\bucketAuthTest.txt");
		String testKeyName = "FlowJoS3PluginBucketAuthTest";
		try {
			// check if one can upload a file to the bucket then delete that
			// file,
			// used for checking the permissions of the bucket
			m_conn.putObject(bucketName, testKeyName, bucketTestFile);
			accessible = true;
			m_conn.deleteObject(bucketName, testKeyName);
		} catch (AmazonServiceException e) {
			System.out.println(e.toString());
			accessible = false;
		}

		return accessible;
	}

	/**
	 * method used to create a bucket on S3
	 *
	 * @param bucketName
	 *            The name of the bucket to be created
	 *
	 * @return Whether or not the bucket was successfully created
	 *
	 */
	public boolean createABucket(String bucketName) {

		boolean bucketCreated = false;

		try {
			m_conn.createBucket(bucketName);
			bucketCreated = true;
		} catch (AmazonClientException e) {
			System.out.println(e.toString());
			bucketCreated = false;
		}

		return bucketCreated;
	}

}