package edu.msu.yangyi20.cloudhatter.Cloud;

import edu.msu.yangyi20.cloudhatter.Cloud.Models.Catalog;
import edu.msu.yangyi20.cloudhatter.Cloud.Models.Hat;
import edu.msu.yangyi20.cloudhatter.Cloud.Models.Item;
import edu.msu.yangyi20.cloudhatter.Cloud.Models.LoadResult;
import edu.msu.yangyi20.cloudhatter.Cloud.Models.SaveResult;
import edu.msu.yangyi20.cloudhatter.HatterView;
import edu.msu.yangyi20.cloudhatter.R;

import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

@SuppressWarnings("deprecation")
public class Cloud {
    private static final String MAGIC = "NechAtHa6RuzeR8x";
    private static final String USER = "yangyi20";
    private static final String PASSWORD = "Zaza1027";
    private static final String BASE_URL = "https://facweb.cse.msu.edu/dennisp/cse476x/";
    public static final String CATALOG_PATH = "hatter-cat.php";
    public static final String SAVE_PATH = "hatter-save.php";
    public static final String DELETE_PATH = "hatter-delete.php";
    public static final String LOAD_PATH = "hatter-load.php";
    private static final String UTF8 = "UTF-8";

    private static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .build();

    /**
     * Open a connection to a hatting in the cloud.
     * @param id id for the hatting
     * @return reference to an input stream or null if this fails
     */
    public Hat openFromCloud(final String id) {
        HatterService service = retrofit.create(HatterService.class);
        try {
            Response<LoadResult> response = service.loadHat(USER, MAGIC, PASSWORD, id).execute();

            // check if request failed
            if (!response.isSuccessful()) {
                Log.e("OpenFromCloud", "Failed to load hat, response code is = " + response.code());
                return null;
            }

            LoadResult result = response.body();
            if (result.getStatus().equals("yes")) {
                return result.getHat();
            }

            Log.e("OpenFromCloud", "Failed to load hat, message is = '" + result.getMessage() + "'");
            return null;
        } catch (IOException | RuntimeException e) {
            Log.e("OpenFromCloud", "Exception occurred while loading hat!", e);
            return null;
        }
    }

    public boolean deleteFromCloud(final String id) {
        HatterService service = retrofit.create(HatterService.class);
        try {
            Response<LoadResult> response = service.deleteHat(USER, MAGIC, PASSWORD, id).execute();
            return true;
        } catch (IOException | RuntimeException e) {
            return false;
        }
    }

    /**
     * An adapter so that list boxes can display a list of filenames from
     * the cloud server.
     */
    public static class CatalogAdapter extends BaseAdapter {

        /**
         * Constructor
         */
        public CatalogAdapter(final View view) {
            // Create a thread to load the catalog
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        catalog = getCatalog();

                        if (catalog.getStatus().equals("no")) {
                            String msg = "Loading catalog returned status 'no'! Message is = '" + catalog.getMessage() + "'";
                            throw new Exception(msg);
                        }
                        if (catalog.getItems().isEmpty()) {
                            String msg = "Catalog does not contain any hattings.";
                            throw new Exception(msg);
                        }

                        view.post(new Runnable() {

                            @Override
                            public void run() {
                                // Tell the adapter the data set has been changed
                                notifyDataSetChanged();
                            }

                        });
                    } catch (Exception e) {
                        // Error condition! Something went wrong
                        Log.e("CatalogAdapter", "Something went wrong when loading the catalog", e);
                        view.post(new Runnable() {
                            @Override
                            public void run() {
                                String string;
                                // make sure that there is a message in the catalog
                                // if there isn't use the message from the exception
                                if (catalog.getMessage() == null) {
                                    string = e.getMessage();
                                } else {
                                    string = catalog.getMessage();
                                }
                                Toast.makeText(view.getContext(), string, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }).start();
        }

        /**
         * The items we display in the list box. Initially this is
         * null until we get items from the server.
         */
        private Catalog catalog = new Catalog("", new ArrayList(), "");

        @Override
        public int getCount() {
            return catalog.getItems().size();
        }

        @Override
        public Item getItem(int position) {
            return catalog.getItems().get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if(view == null) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.catalog_item, parent, false);
            }

            TextView tv = (TextView)view.findViewById(R.id.textItem);
            tv.setText(catalog.getItems().get(position).getName());


            return view;
        }

        // Create a GET query
        public Catalog getCatalog() throws IOException, RuntimeException {
            HatterService service = retrofit.create(HatterService.class);

            Response response = service.getCatalog(USER, MAGIC, PASSWORD).execute();
            // check if request failed
            if (!response.isSuccessful()) {
                Log.e("getCatalog", "Failed to get catalog, response code is = " + response.code());
                return new Catalog("no", new ArrayList<Item>(), "Server error " + response.code());
            }
            Catalog catalog = (Catalog) response.body();
            if (catalog.getStatus().equals("no")) {
                String string = "Failed to get catalog, msg is = " + catalog.getMessage();
                Log.e("getCatalog", string);
                return new Catalog("no", new ArrayList<Item>(), string);
            };
            if (catalog.getItems() == null) {
                catalog.setItems(new ArrayList<Item>());
            }
            return catalog;
        }

        public String getId(int position) {
            return catalog.getItems().get(position).getId();

        }

        public String getName(int position) {
            return catalog.getItems().get(position).getName();

        }

    }

    /**
     * Save a hatting to the cloud.
     * This should be run in a thread.
     * @param name name to save under
     * @param view view we are getting the data from
     * @return true if successful
     */
    public boolean saveToCloud(String name, HatterView view) {
        name = name.trim();
        if(name.length() == 0) {
            return false;
        }

        // Create an XML packet with the information about the current image
        XmlSerializer xml = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        try {
            xml.setOutput(writer);

            xml.startDocument("UTF-8", true);

            xml.startTag(null, "hatter");

            xml.attribute(null, "user", USER);
            xml.attribute(null, "pw", PASSWORD);
            xml.attribute(null, "magic", MAGIC);

            view.saveXml(name, xml);

            xml.endTag(null, "hatter");

            xml.endDocument();

        } catch (IOException e) {
            // This won't occur when writing to a string
            return false;
        }

        HatterService service = retrofit.create(HatterService.class);
        final String xmlStr = writer.toString();
        try {
            Response<SaveResult> response = service.saveHat(writer.toString()).execute();
            if (response.isSuccessful()) {
                SaveResult result = response.body();
                if (result.getStatus() != null && result.getStatus().equals("yes")) {
                    return true;
                }
                Log.e("SaveToCloud", "Failed to save, message = '" + result.getMessage() + "'");
                return false;
            }
            Log.e("SaveToCloud", "Failed to save, message = '" + response.code() + "'");
            return false;
        } catch (IOException e) {
            Log.e("SaveToCloud", "Exception occurred while trying to save hat!", e);
            return false;
        } catch (RuntimeException e) {	// to catch xml errors to help debug step 6
            Log.e("SaveToCloud", "Runtime Exception: " + e.getMessage());
            return false;
        }
    }

}
