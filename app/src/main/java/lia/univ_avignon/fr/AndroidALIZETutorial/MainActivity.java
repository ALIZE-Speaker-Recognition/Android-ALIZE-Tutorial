package lia.univ_avignon.fr.AndroidALIZETutorial;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import AlizeSpkRec.*;
import AlizeSpkRec.SimpleSpkDetSystem.SpkRecResult;

public class MainActivity extends AppCompatActivity {

    private void displayStatus(SimpleSpkDetSystem spkDetSystem) throws Exception {
        Log.i("ALIZE","***********************************************");
        Log.i("ALIZE","System status:");
        Log.i("ALIZE","  # of features: " + spkDetSystem.featureCount());
        Log.i("ALIZE","  # of models: " + spkDetSystem.speakerCount());
        Log.i("ALIZE","  UBM was loaded: " + spkDetSystem.isUBMLoaded());
        Log.i("ALIZE","***********************************************");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            /*====================*/
            /*   Initialization   */
            /*====================*/
            // Phase 1:
            // We create a new speaker recognition system with a config file (extracted from the assets)
            // and a directory where it can store files (models + temporary files).
            InputStream configAsset = getApplicationContext().getAssets().open("AlizeDefault.cfg");
            SimpleSpkDetSystem alizeSystem = new SimpleSpkDetSystem(configAsset, getApplicationContext().getFilesDir().getPath());
            configAsset.close();

            // Phase 2:
            // We load the background model (also from the application assets).
            InputStream backgroundModelAsset = getApplicationContext().getAssets().open("gmm/world.gmm");
            alizeSystem.loadBackgroundModel(backgroundModelAsset);
            backgroundModelAsset.close();

            // The speaker recognition system is now ready to be used.
            displayStatus(alizeSystem);


            /*=========================================*/
            /*   Loading a pre-trained speaker model   */
            /*=========================================*/
            // For some applications, it may make sense to embed pre-built models for some speakers.
            // The most convenient way to distribute such models would be as part of the app assets.
            // Here is an example of how to load an existing model from the assets.
            InputStream speakerModelAsset = getApplicationContext().getAssets().open("gmm/spk02.gmm");
            alizeSystem.loadSpeakerModel("spk02",speakerModelAsset);
            speakerModelAsset.close();

            // Let's check system status.
            displayStatus(alizeSystem);


            /*==================================*/
            /*   Training a new speaker model   */
            /*==================================*/
            // Most applications will include the possibility to register new users and train a speaker model for them.
            // Here is an example of how to do it.

            // Phase 1: Record audio and send it to the system
            // Since audio is usually recorded as 16-bit signed integer linear PCM, ALIZÉ provides a method to add
            // audio at this format, as an array of short ints (whatever the configuration file says about the audio format).
            // This is the method most people would use when recording audio and feeding it to ALIZÉ.
            //
            // Here, we will just simulate recording audio by loading data from the assets into an array of short ints.
            InputStream audioIS = getApplicationContext().getAssets().open("data/audio1.pcm");
            ByteArrayOutputStream audioBytes = new ByteArrayOutputStream();
            while (audioIS.available() > 0) {
                audioBytes.write(audioIS.read());
            }
            byte[] tmpBytes = audioBytes.toByteArray();
            short[] audioL16Samples = new short[tmpBytes.length/2];
            ByteBuffer.wrap(tmpBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(audioL16Samples);

            // Then, pass it to ALIZÉ the same way we would for data just recorded with the microphone.
            alizeSystem.addAudio(audioL16Samples);

            // Phase 2: Train a model with the just-added audio
            // Speaker IDs are just free-form strings. It is usually a good idea to keep them simple and store
            // speaker-related metadata, such as names, in a separate, application-managed database.
            alizeSystem.createSpeakerModel("spk01");

            // Let's check system status.
            displayStatus(alizeSystem);

            // Phase 3:
            // Reset input, since we will not make any more use of this audio signal.
            alizeSystem.resetAudio();
            alizeSystem.resetFeatures();


            /*==============================*/
            /*   Updating a speaker model   */
            /*==============================*/
            // Phase 1: Record some more audio
            // Here, we use the method of SimpleSpkDetSystem that reads directly from an asset input stream.
            // For this approach, the data format in the asset file must match the format specified in the configuration.
            // This method is useful during the development phase of your application, but less likely to be used for real-world usage.
            alizeSystem.addAudio(getApplicationContext().getAssets().open("data/audio2.pcm"));

            // Phase 2: Adapt the model with the just-added audio
            alizeSystem.adaptSpeakerModel("spk01");

            // Phase 3:
            // Reset input, since we will not make any more use of this audio signal.
            alizeSystem.resetAudio();
            alizeSystem.resetFeatures();


            /*============================*/
            /*   Saving a speaker model   */
            /*============================*/
            // Speaker models reside in RAM and are not automatically written to permanent storage.
            // If you want to keep models from one application run to the next (which is usually the case),
            // you need to use the method below for each newly-created or updated model.
            // The first parameter is the speaker ID.
            // For the second parameter, you can either provide a complete, absolute file path to specify where to save the model.
            // Or you can provide a basename (for example, the same string as the speaker ID), in which case the model will be
            // saved along the other ones at the location (and with the filename extension) specified in the configuration file.
            alizeSystem.saveSpeakerModel("spk01", "spk01");


            /*====================================*/
            /*   Verifying a speaker's identity   */
            /*====================================*/
            // This is one of the two main tasks of speaker recognition: compare a recording with a specific
            // speaker model and determine if the two voices match.

            // Phase 1: Record some more audio
            alizeSystem.addAudio(getApplicationContext().getAssets().open("data/audio3.pcm"));

            // Phase 2: Run speaker verification
            // Compare the audio signal with the speaker model we created earlier.
            SpkRecResult verificationResult = alizeSystem.verifySpeaker("spk01");

            // The returned value tells us if there was a match, and what the score was.
            Log.i("ALIZE","***********************************************");
            Log.i("ALIZE","Speaker verification against speaker spk01:");
            Log.i("ALIZE","  match: " + verificationResult.match);
            Log.i("ALIZE","  score: " + verificationResult.score);
            Log.i("ALIZE","***********************************************");

            // We can perform verification for the same audio signal against several speakers
            // without having to reload the signal.
            verificationResult = alizeSystem.verifySpeaker("spk02");

            Log.i("ALIZE","***********************************************");
            Log.i("ALIZE","Speaker verification against speaker spk02:");
            Log.i("ALIZE","  match: " + verificationResult.match);
            Log.i("ALIZE","  score: " + verificationResult.score);
            Log.i("ALIZE","***********************************************");

            // Phase 3:
            // Again, once done with this audio signal, clear the input buffers.
            alizeSystem.resetAudio();
            alizeSystem.resetFeatures();


            /*===========================*/
            /*   Identifying a speaker   */
            /*===========================*/
            // This is the other main task of speaker recognition: compare a recording with all the speakers
            // known to the system in order to determine whose voice it is (or reject it as unknown).

            alizeSystem.addAudio(getApplicationContext().getAssets().open("data/audio4.pcm"));

            SpkRecResult identificationResult = alizeSystem.identifySpeaker();

            // Here, the result tells us whether any registered speaker matched the signal,
            // which one obtained the highest score, and what this score was (even if there was no match).
            Log.i("ALIZE","***********************************************");
            Log.i("ALIZE","Speaker identification:");
            Log.i("ALIZE","  match: " + identificationResult.match);
            Log.i("ALIZE","  closest speaker: " + identificationResult.speakerId);
            Log.i("ALIZE","  score: " + identificationResult.score);
            Log.i("ALIZE","***********************************************");

            alizeSystem.resetAudio();
            alizeSystem.resetFeatures();


            /*================*/
            /*   That's it!   */
            /*================*/

        } catch (Throwable e) {
            Log.d("ALIZE", "Something went wrong.", e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
