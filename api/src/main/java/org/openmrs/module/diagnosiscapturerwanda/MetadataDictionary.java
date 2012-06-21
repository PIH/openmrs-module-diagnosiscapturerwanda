/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.diagnosiscapturerwanda;


import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;

/**
 * The contents of this file are subject to the OpenMRS Public License
package org.openmrs.module.diagnosiscapturerwanda;

import org.openmrs.api.context.Context;

/**
 * this is the singleton that gets populated at module startup by the activator.
 * To add metadata to the module:  1) add an item here, 2) have the constructor populate the item, 3) add it to the global property diagnosisCaptureRwanda.constants 
 *
 * To access, just call MetadataDictionary.getInstance()...
 */
public final class MetadataDictionary {
	
	protected Log log = LogFactory.getLog(getClass());
	
	private static final class MetadataDictionaryHolder {
		static final MetadataDictionary metadata = new MetadataDictionary();
	}
	
	public static Concept CONCEPT_PRIMARY_CARE_DIAGNOSIS;  // the master list of diagnoses
	
	public static Concept CONCEPT_ICPC_DIAGNOSIS_GROUPING_CATEGORIES;   //icpc-like categories
	public static Concept CONCEPT_ICPC_SYMPTOM_INFECTION_INJURY_DIAGNOSIS;  //icpc-like categories
	
	public static Concept CONCEPT_PRIMARY_CARE_PRIMARY_DIAGNOSIS_CONSTRUCT;
	public static Concept CONCEPT_PRIMARY_CARE_SECONDARY_DIAGNOSIS_CONSTRUCT;
	
	public static Concept CONCEPT_DIAGNOSIS_ORDER; //used in secondary diagnosis only.
	public static Concept CONCEPT_DIAGNOSIS_ORDER_PRIMARY;
	public static Concept CONCEPT_DIAGNOSIS_ORDER_SECONDARY;
	public static Concept CONCEPT_DIAGNOSIS_ORDER_TERTIARY;
		
	public static Concept CONCEPT_DIAGNOSIS_CONFIRMED_SUSPECTED;
	public static Concept CONCEPT_SUSPTECTED;
	public static Concept CONCEPT_CONFIRMED;
		
	public static Concept CONCEPT_DIAGNOSIS_NON_CODED; //text
	
	public static Concept CONCEPT_VITALS_TEMPERATURE;
	public static Concept CONCEPT_VITALS_HEIGHT;
	public static Concept CONCEPT_VITALS_WEIGHT;
	public static Concept CONCEPT_VITALS_SYSTOLIC_BLOOD_PRESSURE;
	public static Concept CONCEPT_VITALS_DIATOLIC_BLOOD_PRESSURE;
	public static Concept CONCEPT_VITALS_BMI;
	
	public static Concept CONCEPT_OTHER_SIGNS_OR_SYMPTOMS;
	
	public static Concept CONCEPT_TREATMENT_OTHER;
	public static Concept CONCEPT_REFERRED_TO;
	
	public final static String SESSION_ATTRIBUTE_WORKSTATION_LOCATION = "PROVIDER_WORKSTATION_LOCATION";
	
	public static MetadataDictionary getInstance(){
		return MetadataDictionaryHolder.metadata;
	}
	
	private MetadataDictionary(){
		 String gpString = Context.getAdministrationService().getGlobalProperty("diagnosisCaptureRwanda.constants");
		 List<String> unfoundItems = new ArrayList<String>();
		 if (gpString != null){
		     Reader reader = new StringReader(gpString);
		     Properties props = new Properties();
		     try {
		         props.load(reader);
		     } catch (Exception ex){
		         throw new RuntimeException("invalid values found in global property rwandahivflowsheet.constants, please correct and try again.");
		     } finally {
		         reader = null;
		     }
		     if (props.size() > 0){
		    	 for (Map.Entry<Object, Object> entry : props.entrySet()) {
		    		 if (((String) entry.getKey()).contains("CONCEPT"))
		    			 setupConcept((String) entry.getKey(), (String) entry.getValue(), unfoundItems);
		    		 //TODO:  other object types other than concept?
		    	 }
		         for (String str: unfoundItems)
		         	log.error("diagnosisCaptureRwanda module could not load the folowing item: " + str + ".  Check the gp diagnosisCaptureRwanda.constants.");
		     }
		 } else
			 throw new RuntimeException("The global property diagnosisCaptureRwanda.constants was not found.");
	}
	
	/**
	 * this sets up Concept metadata in the singleton
	 * @param key
	 * @param props
	 * @param unfoundItems
	 */
	private void setupConcept(String key, String value, List<String> unfoundItems){
    	try {
    		String input = value.trim();
	        if (input != null && !"".equals(input)){
	            Concept c = Context.getConceptService().getConceptByUuid(input);
	        	if (c == null){
	        		try {
	        			c = Context.getConceptService().getConcept(Integer.valueOf(input));
	        		} catch (Exception ex){
	        			//pass, string was not numeric
	        		}
	        	}
	            if (c != null){
	            	//we need to hydrate the collections:
	            	c.getConceptMappings();
	            	c.getNames();
	            	setField(key, c);
	            	return;
	            } else
	            	unfoundItems.add(key);
	        } else
	        	unfoundItems.add(key);
	           
    	} catch (Exception ex){
    		unfoundItems.add(key);
    		//log.warn("RwandaFlowsheetModule unable to load metadata for key " + key + ". Please check your mappings in the global property rwandahivflowsheet.constants.");
    	}
    }
	
	/**
	 * This method uses reflection to set fields in this class for all property keys found in the global property with the same name.
	 * @param fieldName
	 * @param value
	 */
	private void setField(String fieldName, Object value){
		Field field = null;
		try {
			field = this.getClass().getField(fieldName);
		} catch (NoSuchFieldException nsfe){
			log.error(fieldName + " found in the global property is not a field in the MetadataDictionary.  Ignoring.");
			nsfe.printStackTrace();
		}
		if (field != null){
			try {
				field.setAccessible(true);
				field.set(this, value);
				log.debug("DiagnosisCaptureRwanda setting " + field.getName() + " to " + value);
			} catch (Exception ex){
				log.error("Unable to set field " + fieldName);
				ex.printStackTrace();
			}
		} 
		
	}
}
