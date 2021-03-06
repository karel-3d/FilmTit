/*Copyright 2012 FilmTit authors - Karel Bílek, Josef Čech, Joachim Daiber, Jindřich Libovický, Rudolf Rosa, Jan Václ

Copyright (C) 2007 Daniel Naber

This file is part of FilmTit.

FilmTit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2.0 of the License, or
(at your option) any later version.

FilmTit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with FilmTit.  If not, see <http://www.gnu.org/licenses/>.*/

package cz.filmtit.share.tokenizers;


import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.google.common.base.Strings;
import com.google.gwt.regexp.shared.RegExp;


public class CzechSentenceTokenizer extends SentenceTokenizer {



    @Override
    public String nonStandardLetters() { 
        return  "áčďéěíňóřšťúůýžÁČĎÉĚÍŇÓŘŠŤÚŮÝŽ"; 
    }


    @Override
    protected String[] getMonthNames() {
        String[] monthNames = {"leden", "únor", "březen", "duben", "květen", "červen", "červenec", "srpen", "září", "říjen", "listopad", "prosinec",
        "ledna", "února", "března", "dubna", "května", "června", "srpna", "září", "října", "listopadu", "prosince"};
        return monthNames;
    }
   
    @Override
    protected String[] getAbbrevList() {
        String[] abbrevList = {
                //TITLES (seriously, I doubt we will have ONE in the subs)
                "Bc","BcA","Ing","Ing.arch","MUDr","MVDr","MgA","Mgr",
                "JUDr","PhDr","RNDr","PharmDr", "ThLic", "ThDr", "Ph.D", "Th.D", "prof", "doc", "CSc", "DrSc", "dr. h. c", "PaedDr", "Dr" , "PhMr" , "DiS",

                //ABBREVIATIONS
                "abt", "ad", "a.i", "aj", "angl", "anon", "apod", "atd", "atp", "aut", "bd", "biogr", 
                 "b.m", "b.p", "b.r", "cca", "cit", "cizojaz", "c.k", "col", "čes", "čín", "čj", "ed", "facs", "fasc", "fol", "fot", "franc", "h.c", "hist", "hl",
            "hrsg", "ibid", "il", "ind", "inv.č", "jap", "jhdt", "jv", "koed", "kol", "korej", "kl", "krit", "lat", "lit", "m.a", "maď", "mj", "mp", "násl",
            "např", "nepubl", "něm", "no", "nr", "n.s", "okr", "odd", "odp", "obr", "opr", "orig", "phil", "pl", "pokrač", "pol", "port", "pozn", "př.kr", 
            "př.n.l", "přel", "přeprac", "příl", "pseud", "pt", "red", "repr", "resp", "revid", "rkp", "roč", "roz", "rozš", "samost", "sect", 
            "sest", "seš", "sign", "sl", "srv", "stol", "sv", "šk", "šk.ro", "špan", "tab", "t.č", "tis", "tj", "tř", "tzv", "univ", "uspoř", "vol", 
            "vl.jm", "vs", "vyd", "vyobr", "zal", "zejm", "zkr", "zprac", "zvl", "n.p",
            //FROM WIKTIONARY

            "např", "než", "MUDr", "abl", "absol", "adj", "adv", "ak", "ak. sl", "akt", "alch", "amer", "anat", "angl", "anglosas", "arab", "arch", "archit", "arg", "astr", "astrol", "att", "bás", "belg", "bibl", "biol", "boh", "bot", "bulh", "círk", "csl", "č", "čas", "čes", "dat", "děj", "dep", "dět", "dial", "dór", "dopr", "dosl", "ekon", "epic", 
        "etnonym", "eufem", "f", "fam", "fem", "fil", "film", "form", "fot", "fr", "fut", "fyz", "gen", "geogr", "geol", "geom", "germ", "gram", "hebr", "herald", "hist", "hl", "hovor", "hud", "hut", "chcsl", "chem", "ie", "imp", "impf", "ind", "indoevr", "inf", "instr", "interj", "ión", "iron", "it", "kanad", "katalán", "klas", "kniž", "komp", "konj", 
    "konkr", "kř", "kuch", "lat", "lék", "les", "lid", "lit", "liturg", "lok", "log", "m", "mat", "meteor", "metr", "mod", "ms", "mysl", "n", "náb", "námoř", "neklas", "něm", "nesklon", "nom", "ob", "obch", "obyč", "ojed", "opt", "part", "pas", "pejor", "pers", "pf", "pl", "plpf", 
    "práv", "prep", "předl", "přivl", "r", "rcsl", "refl", "reg", "rkp", "ř", "řec", "s", "samohl", "sg", "sl", "souhl", "spec", "srov", "stfr", "střv", "stsl", "subj", "subst", "superl", "sv", "sz", "táz", "tech", "telev", "teol", "trans", "typogr", "var", "vedl", "verb", "vl. jm", "voj", "vok", "vůb", "vulg", "výtv", "vztaž", "zahr", "zájm", "zast", "zejm", 
    "zeměd", "zkr", "zř", "mj", "dl", "atp", "sport", "Mgr", "horn", "MVDr", "JUDr", "RSDr", "Bc", "PhDr", "ThDr", "Ing", "aj", "apod", "PharmDr", "pomn", "ev", "slang", "nprap", "odp", "dop", "pol", "st", "stol", "p. n. l", "před n. l", "n. l", "př. Kr", "po Kr", "př. n. l", "odd", "RNDr", "tzv", "atd", "tzn", "resp", "tj", "p", "br", "č. j", "čj", "č. p", "čp", "a. s", "s. r. o", "spol. s r. o", "p. o", "s. p", "v. o. s", "k. s", "o. p. s", "o. s", "v. r", "v z", "ml", "vč", "kr", "mld", "hod", "popř", "ap", "event", "rus", "slov", "rum", "švýc", "PUNCTUATION. T", "zvl", "hor", "dol", "S.O.S"};
        return abbrevList;
    }


}
