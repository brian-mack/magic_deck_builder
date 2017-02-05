/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SourceCode;

import java.util.*;
import javax.json.JsonArray;
import javax.json.JsonObject;
import SourceCode.CardList.*;

/**
 *
 * @author Brian Mack
 */
public class Cards {
    
    /****************** int ***************************************************/
    int                 cmc             = -1;
    int                 multiverse_id   =  0;
    int                 power           = -1;
    int                 toughness       = -1;
    
    /****************** String ************************************************/
    String              image_name          = null;
    String              image_link          = null;
    String              layout              = null;
    String              mana_cost           = null;
    String              mci_number          = null;
    String              special_power       = null;
    String              special_toughness   = null;
    String              rarity              = null;
    String              name                = null;
    String              text                = null;
    String              type                = null;
    String              id                  = null;
    
    /****************** List<String> ******************************************/
    List<String>        colors           = new ArrayList<>();
    List<String>        names            = new ArrayList<>();
    List<String>        subtypes         = new ArrayList<>();
    List<String>        types            = new ArrayList<>();
    List<String>        abilities        = new ArrayList<>();
    List<String>        color_identity   = new ArrayList<>();
    ArrayList<String>        rulings          = new ArrayList<>();
    
    public Cards(JsonObject cardData)
    {
        int j;
        String power_check,toughness_check;
        
        if(cardData.containsKey("name"))
        {
            this.name = cardData.getJsonString("name").toString();
        }
        
        if(cardData.containsKey("names"))
        {
            for(j = 0; j < cardData.getJsonArray("names").size(); j++)
            {
                this.names.add(cardData.getJsonArray("names").getString(j));
            }
        }
        
        if(cardData.containsKey("cmc"))
        {
            this.cmc = cardData.getJsonNumber("cmc").intValue();
        }
        else
        {
            this.cmc = 0;
        }
        
        if(cardData.containsKey("colorIdentity"))
        {
            for(j = 0; j < cardData.getJsonArray("colorIdentity").size(); j++)
            {
                switch(cardData.getJsonArray("colorIdentity").getString(j))
                {
                    case "W":
                        this.color_identity.add("White");
                        break;
                    case "U":
                        this.color_identity.add("Blue");
                        break;
                    case "B":
                        this.color_identity.add("Black");
                        break;
                    case "R":
                        this.color_identity.add("Red");
                        break;
                    case "G":
                        this.color_identity.add("Green");
                        break;
                    default:
                        break;
                }
            }
        }
        
        if(cardData.containsKey("colors"))
        {
            for(j = 0; j < cardData.getJsonArray("colors").size(); j++)
            {
                this.colors.add(cardData.getJsonArray("colors").getString(j));
            }
        }
        
        if(cardData.containsKey("imageName"))
        {
            this.image_name = cardData.getJsonString("imageName").toString();
        }
        
        if(cardData.containsKey("layout"))
        {
            this.layout = cardData.getJsonString("layout").toString();
        }
        
        if(cardData.containsKey("manaCost"))
        {
            this.mana_cost = cardData.getJsonString("manaCost").toString();
        }
        
        if(cardData.containsKey("mciNumber"))
        {
            this.mci_number = cardData.getString("mciNumber");
        }
        
        if(cardData.containsKey("multiverseid"))
        {
            this.multiverse_id = cardData.getJsonNumber("multiverseid")
                    .intValue();
        }
        
        if(cardData.containsKey("power"))
        {
            power_check = cardData.getString("power");
            //This check is being done for the instances where the power
            //is not an integer value.
            if((power_check.contains("*")) || (power_check.contains(".")))
            {
                this.special_power = power_check;
                this.power = -2;
            }
            else
            {
                this.special_power = null;
                this.power = Integer.parseInt(cardData.getString("power"));
            }
        }
        
        if(cardData.containsKey("toughness"))
        {
            toughness_check = cardData.getString("toughness");
            //This is the same as the power but for toughness.
            if((toughness_check.contains("*")) || (toughness_check.
                    contains(".")))
            {
                this.special_toughness = toughness_check;
                this.toughness=-2;
            }
            else
            {
                this.special_toughness=null;
                this.toughness=Integer.parseInt(cardData
                        .getString("toughness"));
            }
        }
        
        if(cardData.containsKey("rarity"))
        {
            this.rarity=cardData.getJsonString("rarity").toString();
        }
        
        if(cardData.containsKey("subtypes"))
        {
            for(j = 0; j < cardData.getJsonArray("subtypes").size(); j++)
            {
                this.subtypes.add(cardData.getJsonArray("subtypes")
                        .getString(j));
            }
        }
        
        if(cardData.containsKey("text"))
        {
            this.text=cardData.getJsonString("text").toString();
            //getAbilities();
        }
        
        if(cardData.containsKey("type"))
        {
            this.type=cardData.getJsonString("type").toString();
        }
        
        if(cardData.containsKey("types"))
        {
            for(j = 0; j < cardData.getJsonArray("types").size(); j++)
            {
                this.types.add(cardData.getJsonArray("types").getString(j));
            }
        }
        
        if(cardData.containsKey("id"))
        {
            this.id=cardData.getJsonString("id").toString();
        }
        
        if(cardData.containsKey("rulings"))
        {
            JsonArray rules = cardData.getJsonArray("rulings");
            
            this.rulings.ensureCapacity(rules.size());
            for(j = 0; j < rules.size(); j++)
            {
                this.rulings.add(rules.getJsonObject(j).getJsonString("text")
                        .toString());
            }
        }
    }
    
    private void getAbilities()
    {
        String temp;
        
        temp = this.text.replaceAll("\\p{P}", " ");
        List<String> splitText = Arrays.asList(temp.split(" "));
        
        for(String staticAbility : CardList.abilitiesList)
        {
            if(splitText.contains(staticAbility))
            {
                this.abilities.add(staticAbility);
            }
        }
    }
}
