/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SourceCode;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import javax.json.*;
/**
 *
 * @author Brian Mack
 */
public class Update_database 
{
    //This function calls the version_lookup function to read in the current
    //internet version. If the versions are different, it returns false. Otherwise
    //it returns true.
    public static boolean version_check(String current_version,String version_url) throws IOException
    {
       //Check version
        String new_version;

        new_version=version_lookup(version_url);
        //The string in the file will have both a return and a new line at the
        //end of the string. A concatenation is needed here to make the strings
        //equivalent.
        new_version=new_version+"\r\n";
        
        return new_version.equals(current_version);
    }
    
    //This function reads in the database from the internet.
    public static void database(String databaseUrl, String destinationFile) throws IOException 
    {
        File file_check=new File(destinationFile);
        //If file doesn't exist, create file and return null
        
        if(!file_check.exists())
        {
            file_check.createNewFile();
        }
        InputStream is;
        URL url = new URL(databaseUrl);
        URLConnection connection = url.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.29 Safari/537.36");
        is = connection.getInputStream();
        BufferedReader reader = new BufferedReader( new InputStreamReader( is )  );
        FileWriter output=null;
        String line;
        try
        {
            output = new FileWriter(destinationFile);
            line = reader.readLine();
            while (line != null) 
            {
                output.write(line);
                line = reader.readLine();
            }
        }finally {
         if (is != null) {
            is.close();
         }
         if (output != null) {
            output.close();
         }
      }
    }
    
    //This function writes the current to a file. If the file does not exist,
    //the file is created and the current version is set to false. This is done
    //to indicate in the file that the versions are not the same.
    public static String set_version(String version_file)throws IOException 
    {
        File file_check=new File(version_file);
        //If file doesn't exist, create file and return false
        
        if(!file_check.exists())
        {
            file_check.createNewFile();
            try (PrintWriter writer = new PrintWriter(version_file, "UTF-8")) {
                writer.write("false");
            }
            return "false";
        }//File does exist and we need to read in the particular version of the
        //file.
        else
        {
            //Read from version file.
            BufferedReader reader = new BufferedReader( new FileReader (version_file));
            String         line;
            StringBuilder  stringBuilder = new StringBuilder();
            String         ls = System.getProperty("line.separator");

            try 
            {
                line = reader.readLine();
                while( line != null ) 
                {
                    stringBuilder.append( line );
                    stringBuilder.append( ls );
                    line = reader.readLine();
                }

                return stringBuilder.toString();
            } 
            finally 
            {
                reader.close();
            }
        }
    }
    
    //This function checks the internet version and returns the value.
    public static String version_lookup(String version_url) throws MalformedURLException, IOException
    {
        InputStream is;
        URL url = new URL(version_url);
        URLConnection connection = url.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.29 Safari/537.36");
        is = connection.getInputStream();
        BufferedReader reader = new BufferedReader( new InputStreamReader( is )  );
        String line;
        try
        {
            line = reader.readLine();
            return line;
        }
        finally 
        {
            if (is != null) 
            {
                is.close();
            }
        }
    }
    
    //This function reads the json file local to the program and stores the
    //objects in the program.
    public static JsonObject read_in_file(String inputFile) throws IOException
    {
        FileInputStream in;
        
        in = new FileInputStream(inputFile);
        JsonReader read = Json.createReader(in);
        JsonObject object = read.readObject();
        return object;
    }
    
    //This function downloads all images from the internet to be stored locally
    //on the pc. This is done to speed up any computations as well as ensure there
    //are not any issues with mapping image names to images.
    public static String download_images(int multiverse_id, String image_name, Boolean first_run) throws IOException,Exception
    {
        //http://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=368970&type=card
        String new_url="http://gatherer.wizards.com/Handlers/Image.ashx?multiverseid="+multiverse_id+"&type=card";
        String file_name;
        String current_directory;
        String[] temp_image;
        URL url=new URL(new_url);
        File new_directory;
        
        current_directory=System.getProperty("user.dir");
        new_directory=new File(current_directory+"/mtgImages");
        
        if(multiverse_id != -1)
        {
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.29 Safari/537.36");
            file_name=new_directory.toString();
            temp_image=image_name.split("\"");
            file_name=file_name.concat("/"+temp_image[1]+".jpg");
            ByteArrayOutputStream out;
            try (InputStream in = new BufferedInputStream(url.openStream())) {
                out = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int n = 0;
                while (-1!=(n=in.read(buf)))
                {
                    out.write(buf, 0, n);
                }   out.close();
            }
            byte[] response = out.toByteArray();
            try (FileOutputStream fos = new FileOutputStream(file_name)) {
                fos.write(response);
            }
            return file_name;
        }
        return null;
    }
    
    //This function writes the current version to the version file.
    public static void write_version_to_file(String version,String version_file) throws IOException
    {
        try (FileWriter output = new FileWriter(version_file)) {
                output.write(version);
        }
    }
    
    //This function is intended to store all of the image names currently
    //downloaded. The purpose of this is to make sure the user does not need
    //to keep downloading the images every time they run the program or the
    //version changes. Only new images will ever be downloaded.
    public static void write_to_image_name_file(ArrayList<String> image_names,String file_name) throws IOException
    {
        FileWriter output = null;
        File file_check=new File(file_name);
        
        if((!file_check.exists()) && (image_names.size() > 0))
        {
            file_check.createNewFile();
            int i;
            try
            {
                output = new FileWriter(file_name);
                for(i = 0; i < (image_names.size() - 1); i++)
                {
                    output.write(image_names.get(i)+",");
                }
                output.write(image_names.get(i));
            }
            finally 
            {
                if (output != null) 
                {
                    output.close();
                }
            }
        }
        else
        {
            System.out.println(!file_check.exists() + ", file does exist. \n"
                    + "Number of downloaded images:" + image_names.size());
        }
    }
    
    //This function reads in the image names and stores them in an array.
    public static ArrayList<String> read_in_image_text_file(String text_file) throws FileNotFoundException, IOException
    {
        File file_check=new File(text_file);
        if(file_check.exists())
        {
            BufferedReader reader = new BufferedReader( new FileReader (text_file));
            String line;
            ArrayList<String> read_in_strings=new ArrayList<>();
            String[] individual_strings;
            int i;

                try 
                {
                    line = reader.readLine();
                    while( line != null ) 
                    {
                        individual_strings=line.split(",");
                        for(i=0;i<individual_strings.length;i++)
                        {
                            read_in_strings.add(individual_strings[i]);
                        }
                        line = reader.readLine();
                    }

                    return read_in_strings;
                } 
                finally 
                {
                    reader.close();
                }
        }
        else
        {
            return null;
        }
    }
}

