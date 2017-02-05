/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SourceCode;

import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.imageio.ImageIO;
import javax.json.*;
import static SourceCode.Update_database.*;
import static SourceCode.InitialProgressBar.*;

/**
 *
 * @author Brian Mack
 */
public class CardList {
    
    public CardList() throws Exception
    {
        /*Read version from local file.
         * If no local file exists, create new local file and set
         * version to null.
         * Else, open local file to read and read in the current version.
         * Set version and do a version check with the version read
         * read online at "http://mtgjson.com/json/version.json".
         */
        try
        {
            this.version = set_version(version_file);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        /* No local file exists. Set upToDateCardList to false
         * Run version lookup to set a current version
         * Download the database from "http://mtgjson.com/json/AllSets.json".
         * Database function will create a new file if none exists.
         * The file is then read into the program and stored in memory
         */
        if(version.equals("false"))
        {
            String current_directory;
            File new_directory = null;
            Boolean check;

            System.out.println("version is " + version);
            this.upToDateCardList = false;
            this.first_run = true;
            current_directory = System.getProperty("user.dir");
            new_directory = new File(current_directory+"/mtgImages");
            check = new_directory.mkdirs();
            System.out.println(check + " Folder was created at " + new_directory);
            if(check)
            {
                System.out.println("File was created. Looking up version.");
                // Get the version from the online source
                this.version = version_lookup(version_url);
                System.out.println("Version found, writing to file.");
                // Write the version to the file created previously overwriting
                // the false mark.
                write_version_to_file(this.version, version_file);
                System.out.println("File was created. Version successfully written. Downloading database now.");
                // Download database from internet source
                database(database_url, destination_file);
                System.out.println("Database downloaded. Reading in file.");
                // Read in database making a virtual copy for the program
                this.database = read_in_file(destination_file);
                System.out.println("Database read. Populating cards.");
                populate_cards();
                System.out.println("Cards populated. Writing image names to file.");
                write_to_image_name_file(this.current_downloads,
                        image_names_file);
            }
            else
            {
                System.out.println("Failed to create folder.");
                System.exit(0);
            }
            System.out.println("Complete");
        }
        else
        {
            this.first_run=false;
            /*Check current version of database against the online value.
             * If returns true, database is up to date
             * Else the database needs to be updated as well as the version file
             * and images file/links.
             */
            this.upToDateCardList = version_check(this.version,version_url);
            if(upToDateCardList)
            {
                System.out.println("Database is up to date");
                this.database=read_in_file(destination_file);
                System.out.println("Reading in image text file");
                this.current_downloads=read_in_image_text_file(image_names_file);
                System.out.println("Populating cards");
                populate_cards();
                System.out.println("Cards populated");
            }
            else
            {
                //Update database
                System.out.println("Updating database");
                database(database_url, destination_file);
                //Set new version
                System.out.println("Version file has been set");
                this.version = version_lookup(version_url);
                //Write to version file
                write_version_to_file(this.version, version_file);
                //Read in database from local file
                System.out.println("Database is up to date");
                this.database = read_in_file(destination_file);
                //Parse database and create individual card objects
                //Set all card values
                System.out.println("Populating Card Objects now");
                populate_cards();
                //Set downloaded images text file
                System.out.println("Writing image names to image file");
                write_to_image_name_file(this.current_downloads,image_names_file);
            }
        }
    }
    /* Images will be stored in a local folder after downloading them from the 
     * internet. The objects for each card will reference the local folder 
     * image name. Online images will only be requested after the database
     * requires updating.
     */
    
    private void populate_cards() throws IOException
    {
        int i,k;
        
        String current_directory;
        
        
        File new_directory;
        String file_name;
        String[] temp_image;
        String setKey;
        // Used to temporarily store card data.
        Object[] jsonKeys;
        ArrayList<String> usedNames = new ArrayList<>();
        JsonArray cardArray;
        
        current_directory=System.getProperty("user.dir");
        new_directory=new File(current_directory+"/mtgImages");
        InitialProgressBar progressBar = new InitialProgressBar();
        
        progressBar.setVisible(true);
        for(i = 0; i < this.database.size(); i++)
        {
            // The file is of the form JsonArray(JsonObject(JsonArray())) 
            // Where the interiror array is a list of cards.
            
            // set is the particular set in the JsonArray that is stored in the 
            // database.
            // Establish the set keys i.e. "LEA" for Limited Edition Alpha
            // This is used to break up the overall JSON Object into the smaller 
            // objects
            
            // jsonKeys is the keys for the given set stored as an array.
            // This is done to get the cards JsonArray.
            jsonKeys = this.database.keySet().toArray();
            while(jsonKeys[i].toString().contains("p"))
            {
                i++;
            }
            setKey = jsonKeys[i].toString();
            // This stores the JsonArray of cards for parsing into card objects.
            cardArray = this.database.getJsonObject(setKey).getJsonArray("cards");
            
            all_cards.ensureCapacity((cardArray.size() + all_cards.size()));
            usedNames.ensureCapacity((cardArray.size() + usedNames.size()));
            progressBar.setMaxValue(all_cards.size());
            
            for(k = 0; k < cardArray.size(); k++)
            {
                JsonObject new_card = cardArray.getJsonObject(k);
                if((new_card.containsKey("name")) 
                    && !usedNames.contains(new_card.getJsonString("name")
                            .toString())  
                    && new_card.containsKey("multiverseid") 
                    && new_card.containsKey("layout") 
                    && (new_card.getJsonString("layout").toString()
                            .contains("normal")
                    || new_card.getJsonString("layout").toString()
                            .contains("split")
                    || new_card.getJsonString("layout").toString()
                            .contains("flip")
                    || new_card.getJsonString("layout").toString()
                            .contains("double-faced")
                    || new_card.getJsonString("layout").toString()
                            .contains("meld")
                    || new_card.getJsonString("layout").toString()
                            .contains("leveler")))
                {
                    usedNames.add(new_card.getJsonString("name").toString());
                    Cards card = new Cards(new_card);
                    all_cards.add(card);
                    //This checks to see if the particular card being parsed
                    //has a downloaded image. If not, the image is downloaded.
                    //Each card is its own seperate object and is stored in the
                    //array. This is done after the card has been completely
                    //populated.
                    if((this.first_run) || (!this.current_downloads.
                            contains(card.image_name)))
                    {
                        try {
                            this.current_downloads.add(card.image_name);
                            card.image_link = download_images(card.multiverse_id
                                    ,card.image_name,first_run);
                        } catch (Exception ex) {
                            Logger.getLogger(CardList.class.getName()).
                                    log(Level.SEVERE, null, ex);
                        }
                    }
                    else
                    {
                        file_name = new_directory.toString();
                        //The image name needs to be stripped of the quotes to be
                        //added to the path of the file name.
                        temp_image = card.image_name.split("\"");
                        file_name = file_name.concat("/"+temp_image[1]+".jpg");
                        card.image_link = file_name;
                    }
                    progressBar.updateValue();
                }
            }
        }
        progressBar.setVisible(false);
    }
    
    public void sortByName(ArrayList<Cards> sorting_array)
    {
        int array_size = sorting_array.size();
        int i, j, k, comparison;
        Cards temp;
        for(i = (array_size / 2); i > 0; i = (i / 2))
        {
            for(j = i; j < array_size; j++)
            {
                for(k = (j - i); k >= 0;k = (k - i))
                {
                    comparison = 
                    sorting_array.get((k+i)).name.compareToIgnoreCase
                        (sorting_array.get(k).name);
                    if(comparison > 0)
                    {
                        break;
                    }
                    else if(comparison < 0)
                    {
                        temp = sorting_array.get(k);
                        sorting_array.set(k, sorting_array.get(k+i));
                        sorting_array.set(k + i, temp);
                    }
                }
            }
        }
    }
    
    public void sortByName(List<Cards> sorting_array)
    {
        int array_size = sorting_array.size();
        int i, j, k, comparison;
        Cards temp;
        for(i = (array_size / 2); i > 0; i = (i / 2))
        {
            for(j = i; j < array_size; j++)
            {
                for(k = (j - i); k >= 0;k = (k - i))
                {
                    comparison = 
                    sorting_array.get((k+i)).name.compareToIgnoreCase
                        (sorting_array.get(k).name);
                    if(comparison > 0)
                    {
                        break;
                    }
                    else if(comparison < 0)
                    {
                        temp = sorting_array.get(k);
                        sorting_array.set(k, sorting_array.get(k+i));
                        sorting_array.set(k + i, temp);
                    }
                }
            }
        }
    }
    
    //Fill type arrays from sorted list
    public void populateSubArrays()
    {
        for(Cards card : this.all_cards)
        {
            sortIntoSubArrays(card);
        }
    }
    
    public void sortIntoSubArrays(Cards card)
    {
        // Colored Card
        if(card.color_identity.contains("White"))
        {
            this.white_cards.add(card);
        }
        if(card.color_identity.contains("Blue"))
        {
            this.blue_cards.add(card);
        }
        if(card.color_identity.contains("Black"))
        {
            this.black_cards.add(card);
        }
        if(card.color_identity.contains("Red"))
        {
            this.red_cards.add(card);
        }
        if(card.color_identity.contains("Green"))
        {
            this.green_cards.add(card);
        }
    }
    
    /***************************************************************************
     * The filter will operate by first collecting the colors that are being
     * added to the list. If no color is selected, then all cards will be added.
     * The next step will be to filter by type. This will be done in a separate
     * function. The next step will be to filter them based on rarity. Finally,
     * the list will be filtered by text. This will be done using both the text
     * used in the search filter as well as a dictionary reference based on user
     * input. For instance, "tap" will be translated to {T} as well as used as 
     * the original text. This will allow for the user to search based on common
     * terms used to describe card abilities. Once the filtering is complete, 
     * the finished list will be stored in a public variable for display on the 
     * GUI.
     **************************************************************************/
    public void searchByFilter(SearchFilter filter)
    {
       // Populate resultsList with search results
        List<Cards> temp = new ArrayList<>();
        
        // Add colors based on filter selection
        if(!filter.colors.isEmpty())
        {
            addColors(temp, filter);
        }
        else
        {
            temp.addAll(this.all_cards);
        }
        // Remove elements based on the type if the type index is empty
        if(!(filter.type.equals("")))
        {
            removeByType(temp, filter);
        }
        // Filter by rarity
        if(!(filter.rarity == null))
        {
            removeByRarity(temp, filter);
        }
        // Filter by name
        if(!(filter.name.equals("")))
        {
            filterByName(temp, filter);
        }
        // Filter by text
        // TODO Add logic to allow for a dictionary
        if(!(filter.text.equals("")))
        {
            filterByText(temp, filter);
        }
        
        /*if(!filter.abilities.isEmpty())
        {
            removeByAbilities(temp, filter);
        }*/
        
        if(!(filter.subType == null) && !(filter.subType.equals("")))
        {
            removeBySubType(temp, filter);
        }
        
        /*if(!(filter.cmc.equals("")) || (filter.intcmc != Integer.MAX_VALUE))
        {
            removeByCMC(temp, filter);
        }
        
        if(!(filter.power.equals("")) || (filter.intpower != Integer.MAX_VALUE))
        {
            removeByPower(temp, filter);
        }
        
        if(!(filter.toughness.equals("")) || (filter.inttoughness != Integer.MAX_VALUE))
        {
            removeByToughness(temp, filter);
        }*/
        
        this.results_list = temp;
    }
    
    private void addColors(List<Cards> currentList, SearchFilter filter)
    {
        if(filter.colorRefinement.contains("selected"))
        {
            if(filter.colors.contains("White"))
            {
                currentList.addAll(this.white_cards);
            }
            if(filter.colors.contains("Blue"))
            {
                currentList.addAll(this.blue_cards);
            }
            if(filter.colors.contains("Black"))
            {
                currentList.addAll(this.black_cards);
            }
            if(filter.colors.contains("Red"))
            {
                currentList.addAll(this.red_cards);
            }
            if(filter.colors.contains("Green"))
            {
                currentList.addAll(this.green_cards);
            }
        }
        else
        {
            if(!filter.colors.contains("White"))
            {
                currentList.addAll(this.white_cards);
            }
            if(!filter.colors.contains("Blue"))
            {
                currentList.addAll(this.blue_cards);
            }
            if(!filter.colors.contains("Black"))
            {
                currentList.addAll(this.black_cards);
            }
            if(!filter.colors.contains("Red"))
            {
                currentList.addAll(this.red_cards);
            }
            if(!filter.colors.contains("Green"))
            {
                currentList.addAll(this.green_cards);
            }
        }
    }
    
    private void removeByType(List<Cards> currentList, SearchFilter filter)
    {
        Iterator<Cards> iter;
        Cards temp;
        //Currently the filter type is at position 3 in the filter. This is a 
        //guaranteed placement until converted to an object.
        String filterType = filter.type;
        iter = currentList.iterator();
        
        for( ; iter.hasNext(); )
        {
            temp = iter.next();
            if(temp.types.contains(filterType) || temp.type.contains(filterType))
            {
            }
            else
            {
                iter.remove();
            }
        }
    }
    
    private void removeByRarity(List<Cards> currentList, SearchFilter filter)
    {
        Iterator<Cards> iter;
        Cards temp;
        //Currently the filter type is at position 3 in the filter. This is a 
        //guaranteed placement until converted to an object.
        String filterRarity = filter.rarity.toLowerCase();
        iter = currentList.iterator();
        
        for( ; iter.hasNext(); )
        {
            temp = iter.next();
            if(temp.rarity.toLowerCase().contains(filterRarity))
            {
            }
            else
            {
                iter.remove();
            }
        }
    }
    
    private void removeByAbilities(List<Cards> currentList, SearchFilter filter)
    {
        Iterator<Cards> iter;
        Cards temp;
        //Currently the filter type is at position 3 in the filter. This is a 
        //guaranteed placement until converted to an object.
        List<String> filterAbility = new ArrayList<>();
        filterAbility.addAll(filter.abilities);
        iter = currentList.iterator();
        int check;
        
        for( ; iter.hasNext(); )
        {
            check = 0;
            temp = iter.next();
            for(String ability : filterAbility)
            {
                for(String tempAbility : temp.abilities)
                {
                    if(tempAbility.compareTo(ability) == 0)
                    {
                        check = 1;
                        break;
                    }
                    
                }
            }
            if(check == 0)
            {
                iter.remove();
            }
        }
    }
    
    private void removeBySubType(List<Cards> currentList, SearchFilter filter)
    {
        Iterator<Cards> iter;
        Cards temp;
        //Currently the filter type is at position 3 in the filter. This is a 
        //guaranteed placement until converted to an object.
        String filterSubType = filter.subType;
        iter = currentList.iterator();
        
        for( ; iter.hasNext(); )
        {
            temp = iter.next();
            if(!temp.subtypes.contains(filterSubType))
            {
                iter.remove();
            }
        }
    }
        
    private void filterByName(List<Cards> currentList, SearchFilter filter)
    {
        Iterator<Cards> iter;
        Cards temp;
        //Currently the filter type is at position 3 in the filter. This is a 
        //guaranteed placement until converted to an object.
        String filterName = filter.name.toLowerCase();
        iter = currentList.iterator();
        
        for( ; iter.hasNext(); )
        {
            temp = iter.next();
            if(temp.name.toLowerCase().contains(filterName))
            {
            }
            else
            {
                iter.remove();
            }
        }
    }
    
    private void filterByText(List<Cards> currentList, SearchFilter filter)
    {
        Iterator<Cards> iter;
        Cards temp;
        //Currently the filter type is at position 3 in the filter. This is a 
        //guaranteed placement until converted to an object.
        String filterText = filter.text.toLowerCase();
        iter = currentList.iterator();
        
        for( ; iter.hasNext(); )
        {
            temp = iter.next();
            if((temp.text != null) && !(temp.text.isEmpty()))
            {
                if(temp.text.toLowerCase().contains(filterText))
                {
                }
                else
                {
                    iter.remove();
                }
            }
            else
            {
                iter.remove();
            }
        }
    }
    
    //Variable Declarations
    private Boolean upToDateCardList    = false;
    private Boolean accessWebSites  = false;
    private String version_file = "version.txt";
    private String version_url = "http://mtgjson.com/json/version.json";
    private String database_url = "http://mtgjson.com/json/AllSets-x.json";
    private String destination_file = "AllSets-x.json";
    static public String[] abilitiesList = {"<none>", "Absorb", "Affinity", "Amplify",
        "Annihilator", "Attach", "Aura swap", "Banding", "Bands with other",
        "Battalion", "Battle cry", "Bestow", "Bloodrush", "Bloodthirst", 
        "Bolster", "Bury", "Bushido", "Buyback", "Cascade", "Champion", 
        "Changeling", "Channel", "Chroma", "Cipher", "Clash", "Conspire", 
        "Convoke", "Counter", "Cumulative upkeep", "Cycling", "Dash", 
        "Deathtouch", "Defender", "Delve", "Detain", "Devour", "Domain",
        "Double strike", "Dredge", "Echo", "Enchant", "Entwine", "Epic", 
        "Equip", "Evoke", "Evolve", "Exalted", "Exile", "Exploit", "Extort", 
        "Fading", "Fateful hour", "Fateseal", "Fear", "Ferocious", "Fight", 
        "First strike", "Flanking", "Flash", "Flashback", "Flip", "Flying", 
        "Forecast", "Fortify", "Frenzy", "Graft", "Grandeur", "Gravestorm", 
        "Haste", "Haunt", "Hellbent", "Heroic", "Hexproof", "Hideaway", 
        "Horsemanship", "Imprint", "Indestructible", "Infect", "Intimidate", 
        "Join forces", "Kicker", "Kinship", "Landfall", "Landwalk", "Level up",
        "Lifelink", "Living weapon", "Madness", "Manifest", "Menace", 
        "Metalcraft", "Miracle", "Modular", "Monstrosity", "Morbid", "Morph", 
        "Multikicker", "Ninjutsu", "Offering", "Overload", "Persist", "Phasing",
        "Poisonous", "Populate", "Proliferate", "Protection", "Provoke", 
        "Prowess", "Prowl", "Radiance", "Raid", "Rampage", "Reach", "Rebound", 
        "Recover",  "Regenerate",  "Reinforce",  "Renown",  "Replicate",  
        "Retrace",  "Ripple",  "Sacrifice",  "Scavenge",  "Scry", "Shadow", 
        "Shroud", "Soulbond", "Soulshift", "Splice", "Split second", "Storm", 
        "Substance", "Sunburst", "Suspend", "Sweep", "Tap", "Untap", 
        "Threshold", "Totem armor", "Trample", "Transfigure", "Transform", 
        "Transmute", "Typecycling", "Undying", "Unearth", "Unleash", 
        "Vanishing", "Vigilance", "Wither"};
    private String image_names_file = "image_file.txt";
    private String version;
    private Boolean first_run;
    private JsonObject database;
    private ArrayList<String> current_downloads = new ArrayList<>();
    List<String> filters = new ArrayList<>();
    //Sub arrays of all_cards
    public ArrayList<Cards> all_cards       = new ArrayList<>();
    public List<Cards> blue_cards           = new ArrayList<>();
    public List<Cards> green_cards          = new ArrayList<>();
    public List<Cards> red_cards            = new ArrayList<>();
    public List<Cards> white_cards          = new ArrayList<>();
    public List<Cards> black_cards          = new ArrayList<>();
    public List<Cards> artifact_cards       = new ArrayList<>();
    public List<Cards> land_cards           = new ArrayList<>();
    public List<Cards> planeswalker_cards   = new ArrayList<>();
    public List<Cards> results_list         = new ArrayList<>();
}
