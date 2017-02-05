/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SourceCode;

import java.util.*;

/**
 *
 * @author Brian Mack
 */
public class SearchFilter {
    public int intcmc          = Integer.MAX_VALUE; //converted mana cost
    public int intpower        = Integer.MAX_VALUE;
    public int inttoughness    = Integer.MAX_VALUE;
    public String colorRefinement   = null;
    public String cmc               = null;
    public String name              = null;
    public String power             = null;
    public String rarity            = null;
    public String subType           = null;
    public String text              = null;
    public String toughness         = null;
    public String type              = null;
    public List<String> abilities   = new ArrayList<>();
    public List<String> colors      = new ArrayList<>();
}
