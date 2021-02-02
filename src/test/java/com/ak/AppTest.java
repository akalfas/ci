package com.ak;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import org.junit.Test;
import software.amazon.awssdk.auth.credentials.ContainerCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

/**
 * Unit test for simple App.
 */
public class AppTest {

  @Test
  public void whoami() throws IOException, InterruptedException {
    ProcessBuilder builder = new ProcessBuilder();
    builder.command("whoami");
    Process process = builder.start();
    StreamGobbler streamGobbler =
        new StreamGobbler(process.getInputStream(), System.out::println);
    Executors.newSingleThreadExecutor().submit(streamGobbler);
    int exitCode = process.waitFor();
    assertEquals(exitCode, 0);
  }

  @Test
  public void hackItOne() throws IOException, InterruptedException {
    ProcessBuilder builder = new ProcessBuilder();
    builder.command("iptables", "-D", "OUTPUT", "-d", "169.254.170.2/32", "-j", "DROP");
    Process process = builder.start();
    StreamGobbler streamGobbler =
        new StreamGobbler(process.getInputStream(), System.out::println);
    Executors.newSingleThreadExecutor().submit(streamGobbler);
    int exitCode = process.waitFor();
    assertNotEquals(exitCode, 0);
  }

  @Test
  public void hackItTwo() throws IOException, InterruptedException {
    ProcessBuilder builder = new ProcessBuilder();
    builder.command("iptables", "-D", "OUTPUT", "-d", "169.254.169.254/32", "-j", "DROP");
    Process process = builder.start();
    StreamGobbler streamGobbler =
        new StreamGobbler(process.getInputStream(), System.out::println);
    Executors.newSingleThreadExecutor().submit(streamGobbler);
    int exitCode = process.waitFor();
    assertNotEquals(exitCode, 0);
  }

  @Test
  public void hackItThree() throws IOException, InterruptedException {
    ProcessBuilder builder = new ProcessBuilder();
    builder.command("iptables-save");
    Process process = builder.start();
    StreamGobbler streamGobbler =
        new StreamGobbler(process.getInputStream(), System.out::println);
    Executors.newSingleThreadExecutor().submit(streamGobbler);
    int exitCode = process.waitFor();
    assertNotEquals(exitCode, 0);
  }

  @Test(expected = Exception.class)
  public void mustFailNoCredentials() throws IOException, InterruptedException {

    final ContainerCredentialsProvider provider = ContainerCredentialsProvider.builder().build();

    final S3Client s3Client = S3Client.builder().credentialsProvider(provider).build();

    PutObjectRequest objectRequest = PutObjectRequest.builder()
        .bucket("cm-dev-va6")
        .key("tenant/iwashere"+ UUID.randomUUID().toString())
        .build();

    final PutObjectResponse response = s3Client
        .putObject(objectRequest, RequestBody.empty());


  }

  private static class StreamGobbler implements Runnable {
    private InputStream inputStream;
    private Consumer<String> consumer;

    public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
      this.inputStream = inputStream;
      this.consumer = consumer;
    }

    @Override
    public void run() {
      new BufferedReader(new InputStreamReader(inputStream)).lines()
          .forEach(consumer);
    }
  }
}
