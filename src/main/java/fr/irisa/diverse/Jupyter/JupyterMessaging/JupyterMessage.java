package fr.irisa.diverse.Jupyter.JupyterMessaging;

import fr.irisa.diverse.Core.Kernel;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Data structure of a Jupyter message.
 * The documentation is available here : http://jupyter-client.readthedocs.io/en/latest/messaging.html#general-message-format
 *
 * Created by antoine on 10/05/2017.
 */
class JupyterMessage {

    /* =================================================================================================================
                                               ATTRIBUTES
     =================================================================================================================*/

    private Kernel kernel = null;
    private JSONObject message;

    // Main elements of the message
    private final int JUPYTER_MESSAGE_LENGTH = 7;
    private String uuid;
    private String hmac;
    private String delimiter;
    private JSONObject header;
    private JSONObject parent_header;
    private JSONObject metadata;
    private JSONObject content;

    /* =================================================================================================================
                                               CONSTRUCTORS
     =================================================================================================================*/

    /**
     * Constructor with minimal number of arguments
     */
    public JupyterMessage(Kernel kernel, String msg_type) {
        this.kernel = kernel;

        String msg_id = UUID.randomUUID().toString();
        String username = kernel.getContainerId();
        String session = kernel.getSession();
        String date = generateDate(); // ISO 8061 compliant timestamp
        uuid = "";
        delimiter = "<IDS|MSG>";

        // Build the header
        header = new JSONObject();
        header.put("msg_id", msg_id);
        header.put("username", username);
        header.put("session", session);
        header.put("date", date);
        header.put("msg_type", msg_type);
        header.put("version", "5.1");

        // Initialize other attributes
        parent_header = new JSONObject();
        metadata = new JSONObject();
        content = new JSONObject();
    }

    /**
     * Complete constructor for message to send
     * @param kernel : source kernel
     * @param msg_type : the type of message to send
     * @param parent_header :  dict
     * @param metadata :  dict
     * @param content : dict
     */
    public JupyterMessage(Kernel kernel, String msg_type, JSONObject parent_header, JSONObject metadata, JSONObject content) {
        this(kernel, msg_type);
        if (parent_header != null) this.parent_header = parent_header;
        if (metadata != null) this.metadata = metadata;
        if (content != null) this.content = content;
    }

    /** Constructor for incoming messages
     *
     * @param incomingMessage : Array of String containing the parts of the message
     */
    public JupyterMessage(Kernel kernel, ArrayList<String> incomingMessage) {
        // Store the source kernel instance
        this.kernel = kernel;

        // Create the message from the received data
        message = new JSONObject();

        if(incomingMessage.size() == JUPYTER_MESSAGE_LENGTH) {
            message.put("uuid", incomingMessage.get(0));
            this.uuid = incomingMessage.get(0);
            message.put("delimiter", incomingMessage.get(1));
            this.delimiter = incomingMessage.get(1);
            message.put("hmac", incomingMessage.get(2));
            this.hmac = incomingMessage.get(2);
            message.put("header", incomingMessage.get(3));
            message.put("parent_header", incomingMessage.get(4));
            message.put("metadata", incomingMessage.get(5));
            message.put("content", incomingMessage.get(6));

            // Construct the header, parent_header, metadata and content to make them easily accessible
            JSONParser jsonParser = new JSONParser();
            try {
                header = (JSONObject) jsonParser.parse(incomingMessage.get(3));
                parent_header = (JSONObject) jsonParser.parse(incomingMessage.get(4));
                metadata = (JSONObject) jsonParser.parse(incomingMessage.get(5));
                content = (JSONObject) jsonParser.parse(incomingMessage.get(6));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Incoming message error : missing informations");
        }

    }

    /* =================================================================================================================
                                               GETTER AND SETTERS
     =================================================================================================================*/

    /**
     * Get the header of the message as a JSONObject.
     * The header contains : String msg_id, String username, String session, String date, String msg_type,
     * String version="5.0"
     *
     * @return {JSONObject} the header of the message.
     */
    public JSONObject getHeader () { return this.header; }

    /**
     * Set the parent header of the message.
     *
     * Use it when the message you create respond to another message.
     *
     * The parent_header must contain : String msg_id, String username, String session, String date, String msg_type,
     * String version="5.0"
     *
     * @param parent_header {JSONObject} the parent header
     */
    public void setParentHeader (JSONObject parent_header) {
        this.parent_header = parent_header;
    }

    /**
     * Get the parent header of the message.
     *
     * The parent_header must contain : String msg_id, String username, String session, String date, String msg_type,
     * String version="5.0"
     *
     * @return {JSONObject} the parent header
     */
    public JSONObject getParentHeader () { return this.parent_header; }

    /**
     * Set the metadata part of the message.
     * Its content is free.
     *
     * @param metadata {JSONObject} the metadata
     */
    public void setMetadata (JSONObject metadata) {
        this.metadata = metadata;
    }

    /**
     * Get the HMAC of the message
     * @return {String} the HMAC
     */
    public String getHmac () {
        if(this.hmac == null) this.hmac = generateHmac();
        return this.hmac;
    }

    /**
     * Get the metadata of the message.
     * Its content is free.
     *
     * @return {JSONObject} the metadata
     */
    public JSONObject getMetadata () { return this.metadata; }

    /**
     * Get the universally unique identifier (UUID) of the message
     * @return {String} the UUID of the message
     */
    public String getUuid () { return uuid; }

    /**
     * Set the content part of the message. It must be a JSON.
     * Its content depends on the type of message, according to this documentation :
     * http://jupyter-client.readthedocs.io/en/latest/messaging.html
     *
     * @param content {JSONObject} the content part.
     */
    public void setContent (JSONObject content) {
        this.content = content;
    }

    /**
     * Get the content part of the message. It must be a JSON.
     * Its content depends on the type of message, according to this documentation :
     * http://jupyter-client.readthedocs.io/en/latest/messaging.html
     *
     * @return {JSONObject} the content of the message.
     */
    public JSONObject getContent () { return this.content; }

    /**
     * Get the message serialized in the proper format in order to send it through the channel,
     * using the ZMQ library.
     *
     * @return {String[]} the serialized message
     */
    public String[] getMessageToSend () {
        buildMessage();

        // Add each field in the right order and respect the way python list are built
        String[] msg = new String[6];
        msg[0] = this.message.get("delimiter").toString();
        msg[1] = this.message.get("hmac").toString();
        msg[2] = this.message.get("header").toString();
        msg[3] = this.message.get("parent_header").toString();
        msg[4] = this.message.get("metadata").toString();
        msg[5] = this.message.get("content").toString();

        return msg;
    }

    /* =================================================================================================================
                                                CUSTOM METHODS
     =================================================================================================================*/

    /**
     * Generate an ISO 8061 compliant timestamp
     * @return : String - the timestamp
     */
    private String generateDate () {
        TimeZone tz = TimeZone.getTimeZone("GMT+1");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.ssssss'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tz);
        return df.format(new Date());
    }

    /**
     * Generate the Jupypter messaging protocol compliant hmac
     * @return : a hmac for the message to send
     */
    private String generateHmac() {
        final String ALGORITHM = "HmacSHA256";

        String result = "";

        try {
            Mac hmac = Mac.getInstance(ALGORITHM);
            SecretKeySpec sk = new SecretKeySpec(kernel.getKey().getBytes(), ALGORITHM);
            hmac.init(sk);
            hmac.update(header.toJSONString().getBytes());
            hmac.update(parent_header.toJSONString().getBytes());
            hmac.update(metadata.toJSONString().getBytes());
            hmac.update(content.toJSONString().getBytes());
            byte[] mac_data = hmac.doFinal();

            // Convert the hmac into a String to send it
            for (final byte element : mac_data)
            {
                result += Integer.toString((element & 0xff) + 0x100, 16).substring(1);
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Build the message in accordance with Jupyter messaging specification
     */
    private void buildMessage () {
        // Build the message
        message = new JSONObject();
        message.put("uuid", uuid);
        message.put("delimiter", delimiter);
        message.put("header", header);

        message.put("parent_header", parent_header);
        message.put("metadata", metadata);
        message.put("content", content);

        // Generate and add the hmac
        hmac = generateHmac();
        message.put("hmac", hmac);
    }

    @Override
    public String toString() {
        buildMessage();

        return message.toJSONString();
    }

}
