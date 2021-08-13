package edu.harvard.cs50.pokedex;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class PokemonActivity extends AppCompatActivity
{
    private TextView nameTextView;
    private TextView numberTextView;
    private TextView type1TextView;
    private TextView type2TextView;
    private String url;
    private String descriptionUrl;
    private RequestQueue requestQueue;
    private Boolean caughtState;
    private Button catchButton;
    private ImageView pokemonSprite;
    private TextView descriptionTextView;

    public String getUrl() { return url; }
    public Boolean getCaughtState()
    {
        return caughtState;
    }
    public void setCaughtState(Boolean caughtState)
    {
        this.caughtState = caughtState;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon);

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        url = getIntent().getStringExtra("url");
        descriptionUrl = "https://pokeapi.co/api/v2/pokemon-species/".concat(getIntent().getStringExtra("name"));
        nameTextView = findViewById(R.id.pokemon_name);
        numberTextView = findViewById(R.id.pokemon_number);
        type1TextView = findViewById(R.id.pokemon_type1);
        type2TextView = findViewById(R.id.pokemon_type2);
        catchButton = findViewById(R.id.catch_button);
        pokemonSprite = findViewById(R.id.pokemon_sprite);
        descriptionTextView = findViewById(R.id.pokemon_description);

        load();
    }

    public void load()
    {
        type1TextView.setText("N/A");
        type2TextView.setText("N/A");

        JsonObjectRequest detailsRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                try
                {
                    String pokemonName = response.getString("name");
                    nameTextView.setText(pokemonName.substring(0, 1).toUpperCase() + pokemonName.substring(1));
                    String pokemonNumber = String.format("#%03d", response.getInt("id"));
                    numberTextView.setText(pokemonNumber);

                    JSONArray typeEntries = response.getJSONArray("types");
                    for (int i = 0; i < typeEntries.length(); i++)
                    {
                        JSONObject typeEntry = typeEntries.getJSONObject(i);
                        int slot = typeEntry.getInt("slot");
                        String type = typeEntry.getJSONObject("type").getString("name");

                        if (slot == 1)
                        {
                            type1TextView.setText(type.substring(0, 1).toUpperCase() + type.substring(1));
                        }
                        else if (slot == 2)
                        {
                            type2TextView.setText(type.substring(0, 1).toUpperCase() + type.substring(1));;
                        }
                    }
                    
                    JSONObject spriteEntries = response.getJSONObject("sprites");
                    String spriteUrl = spriteEntries.getString("front_default");
                    new DownloadSpriteTask().execute(spriteUrl);
                }
                catch (JSONException e)
                {
                    Log.e("cs50", "Pokemon json error", e);
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e("cs50", "Pokemon details error");
            }
        });
        requestQueue.add(detailsRequest);

        JsonObjectRequest descriptionRequest = new JsonObjectRequest(Request.Method.GET, descriptionUrl, null, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                try
                {
                    JSONArray flavorTextEntries = response.getJSONArray("flavor_text_entries");
                    for (int i = 0; i < flavorTextEntries.length(); i++)
                    {
                        JSONObject flavorTextEntry = flavorTextEntries.getJSONObject(i);
                        String language = flavorTextEntry.getJSONObject("language").getString("name");
                        if (language.equals("en"))
                        {
                            String description = flavorTextEntry.getString("flavor_text");
                            descriptionTextView.setText(description);
                            break;
                        }
                    }
                }
                catch (JSONException e)
                {
                    Log.e("cs50", "Pokemon json error", e);
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e("cs50", "Pokemon description error");
            }
        });
        requestQueue.add(descriptionRequest);

        caughtState = getPreferences(Context.MODE_PRIVATE).getBoolean(getUrl(), false);
        if (getCaughtState() == true)
        {
            catchButton.setText("Release");
        }
        else
        {
            catchButton.setText("Catch");
        }
    }

    private class DownloadSpriteTask extends AsyncTask<String, Void, Bitmap>
    {
        @Override
        protected Bitmap doInBackground(String... strings)
        {
            try
            {
                URL url = new URL(strings[0]);
                return BitmapFactory.decodeStream(url.openStream());
            }
            catch (IOException e)
            {
                Log.e("cs50", "Download sprite error", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap)
        {
            pokemonSprite.setImageBitmap(bitmap);
        }
    }

    public void toggleCatch(View view)
    {
        if (getCaughtState() == true)
        {
            getPreferences(Context.MODE_PRIVATE).edit().putBoolean(getUrl(), false).apply();
            setCaughtState(false);
            catchButton.setText("Catch");
        }
        else
        {
            getPreferences(Context.MODE_PRIVATE).edit().putBoolean(getUrl(), true).apply();
            setCaughtState(true);
            catchButton.setText("Release");
        }
    }
}