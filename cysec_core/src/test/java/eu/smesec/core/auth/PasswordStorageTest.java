package eu.smesec.core.auth;

import org.junit.Test;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertTrue;

/**
 * Test class for all password storages
 *
 * Created by martin.gwerder on 17.01.2018.
 */
public class PasswordStorageTest {

    private static final String[][] testTuples=new String[][] {
            { "secret:saltsalt",         "$1$saltsalt$9xy1btjgzLYfb7hivXtC//"},
            { "secret:saltsalt",         "$5$saltsalt$0IyaXrmV7.sGNS6tirgqHLqX/G.FBvgkYA.lpPdS5sA"},
            { "secret:saltsalt",         "$6$saltsalt$TVLlQcbpFVof5W3Yz4DTP6gRstiNuHwwTt6GLc1E5n0U0aDehy0S5knV8wiOQSpT0Y77vwPZN.Pq.H91p5hVO1"},
            { "SecretPassword:SaltSalt", "$1$SaltSalt$FSYmvnuDuSP883uWgYBXW/" },
            { "hello-world:salt",        "$1$salt$pJUW3ztI6C1N/anHwD6MB0"},
            { "password:saltsalt",       "$6$saltsalt$qFmFH.bQmmtXzyBY0s9v7Oicd2z4XSIecDzlB5KiA2/jctKu9YterLp8wwnSq.qc.eoxqOmSuNp2xS0ktL3nh/" },
            { "8D6glF1ryaVG0lSvfm4r:saltsalt",       "$99$saltsalt$8D6glF1ryaVG0lSvfm4r" }
    };

    @Test
    public void staticCryptPasswordCases() throws NoSuchAlgorithmException {
        for( String[] testcase : testTuples) {
            String[] in = testcase[0].split( ":" );
            assertTrue( "testing password \"" + in[0] + "\" with default hash and random salt for verification",     new CryptPasswordStorage(in[0],null).verify(in[0]));
            assertTrue( "testing password \"" + in[0] + "\" with predefined hash and without salt for verification", new CryptPasswordStorage(in[0], in[1]).verify(in[0]));
        }
    }

    @Test
    public void staticCryptStorageCases() throws NoSuchAlgorithmException {
        for( String[] testcase : testTuples ) {
            String[] in = testcase[0].split(":");
            assertTrue( "testing password \"" + in[0] + "\" from storage \"" + testcase[1] + "\"", new CryptPasswordStorage( testcase[1] ).verify(in[0]));
            assertTrue( "testing storage \"" + testcase[1] + "\" from storage \"" + testcase[1] + "\"!=\"" + new CryptPasswordStorage( in[0], in[1], new CryptPasswordStorage(testcase[1]).getType() ).getPasswordStorage() + "\"",new CryptPasswordStorage( in[0], in[1], new CryptPasswordStorage( testcase[1] ).getType()).getPasswordStorage().equals( testcase[1] ) );
        }
    }

    @Test
    public void emptySaltInCryptPasswordStorage() throws NoSuchAlgorithmException {
        for(String[] testcase : testTuples) {
            String[] in = testcase[0].split( ":" );
            try{
                CryptPasswordStorage s = new CryptPasswordStorage( in[0],null );
                assertTrue( "storage did not successfully verify to password", s.verify( in[0] ) );
                assertTrue( "storage did not autogenerate salt (length=" + s.getSalt().length() + ")", s.getSalt().length() > 10 );
            } catch( Exception iae ) {
                fail( "null salt did raise exception" );
            }
            try{
                new CryptPasswordStorage( in[1], "" ).verify( in[0] );
                fail( "empty string salt did not raise exception" );
            } catch( IllegalArgumentException iae ) {
                // this is expected
            }
        }
    }

    @Test
    public void randomStorageInCryptPasswordStorage() {
        SecureRandom random = new SecureRandom();
        int numDollars = random.nextInt( 10 );
        for( int i=0; i<10000; i++ ) {
            // create random storage
            String storage = getRandomHexString( random.nextInt( 120 ) + 10 );
            // inject random dollars
            for(int j=0; j<numDollars; j++) {
                byte[] b = storage.getBytes();
                b[ random.nextInt( b.length ) ] = '$';
                storage = new String( b );
            }
            try {
                new CryptPasswordStorage( storage );
                // may be successful accidentally
            } catch( IllegalArgumentException iae ) {
                // likely to happen as we do not enforce a valid hash type
            } catch( Exception e ) {
                e.printStackTrace();
                fail( "caught unexpeted exception while fuzzing storage" );
            }
        }
    }

    @Test
    public void randomStorageValidAlgorithmInCryptPasswordStorage()  {
        SecureRandom random = new SecureRandom();
        int numDollars = random.nextInt( 10 );
        for( int i=0; i<10000; i++) {
            // create random storage
            String storage = getRandomHexString( random.nextInt( 60 ) + 10 );
            // inject random dollars
            for(int j=0; j<numDollars; j++) {
                byte[] b=storage.getBytes();
                b[ random.nextInt(b.length) ] = '$';
                storage = new String( b );
            }
            storage = "$1$" + storage;
            try {
                new CryptPasswordStorage( storage );
                // may be successful accidentally
            } catch( IllegalArgumentException iae ) {
                // may happen
            } catch( Exception e ) {
                e.printStackTrace();
                fail( "caught unexpeted exception while fuzzing storage" );
            }
        }
    }

    private static String getRandomHexString(int length){
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        while(sb.length() < length){
            sb.append( Integer.toHexString( random.nextInt() ) );
        }

        return sb.toString().substring( 0, length );
    }
}
