
/* First created by JCasGen Wed May 25 15:01:02 CEST 2022 */
package de.fernunihagen.d2l2.newtypes;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Wed May 25 15:01:02 CEST 2022
 * @generated */
public class CoreferenceEntity_Type extends Annotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = CoreferenceEntity.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.fernunihagen.d2l2.newtypes.CoreferenceEntity");
 
  /** @generated */
  final Feature casFeat_corefId;
  /** @generated */
  final int     casFeatCode_corefId;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getCorefId(int addr) {
        if (featOkTst && casFeat_corefId == null)
      jcas.throwFeatMissing("corefId", "de.fernunihagen.d2l2.newtypes.CoreferenceEntity");
    return ll_cas.ll_getIntValue(addr, casFeatCode_corefId);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setCorefId(int addr, int v) {
        if (featOkTst && casFeat_corefId == null)
      jcas.throwFeatMissing("corefId", "de.fernunihagen.d2l2.newtypes.CoreferenceEntity");
    ll_cas.ll_setIntValue(addr, casFeatCode_corefId, v);}
    
  
 
  /** @generated */
  final Feature casFeat_beginPosition;
  /** @generated */
  final int     casFeatCode_beginPosition;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getBeginPosition(int addr) {
        if (featOkTst && casFeat_beginPosition == null)
      jcas.throwFeatMissing("beginPosition", "de.fernunihagen.d2l2.newtypes.CoreferenceEntity");
    return ll_cas.ll_getIntValue(addr, casFeatCode_beginPosition);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setBeginPosition(int addr, int v) {
        if (featOkTst && casFeat_beginPosition == null)
      jcas.throwFeatMissing("beginPosition", "de.fernunihagen.d2l2.newtypes.CoreferenceEntity");
    ll_cas.ll_setIntValue(addr, casFeatCode_beginPosition, v);}
    
  
 
  /** @generated */
  final Feature casFeat_endPosition;
  /** @generated */
  final int     casFeatCode_endPosition;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getEndPosition(int addr) {
        if (featOkTst && casFeat_endPosition == null)
      jcas.throwFeatMissing("endPosition", "de.fernunihagen.d2l2.newtypes.CoreferenceEntity");
    return ll_cas.ll_getIntValue(addr, casFeatCode_endPosition);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setEndPosition(int addr, int v) {
        if (featOkTst && casFeat_endPosition == null)
      jcas.throwFeatMissing("endPosition", "de.fernunihagen.d2l2.newtypes.CoreferenceEntity");
    ll_cas.ll_setIntValue(addr, casFeatCode_endPosition, v);}
    
  
 
  /** @generated */
  final Feature casFeat_name;
  /** @generated */
  final int     casFeatCode_name;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getName(int addr) {
        if (featOkTst && casFeat_name == null)
      jcas.throwFeatMissing("name", "de.fernunihagen.d2l2.newtypes.CoreferenceEntity");
    return ll_cas.ll_getStringValue(addr, casFeatCode_name);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setName(int addr, String v) {
        if (featOkTst && casFeat_name == null)
      jcas.throwFeatMissing("name", "de.fernunihagen.d2l2.newtypes.CoreferenceEntity");
    ll_cas.ll_setStringValue(addr, casFeatCode_name, v);}
    
  
 
  /** @generated */
  final Feature casFeat_firstMention;
  /** @generated */
  final int     casFeatCode_firstMention;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getFirstMention(int addr) {
        if (featOkTst && casFeat_firstMention == null)
      jcas.throwFeatMissing("firstMention", "de.fernunihagen.d2l2.newtypes.CoreferenceEntity");
    return ll_cas.ll_getStringValue(addr, casFeatCode_firstMention);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setFirstMention(int addr, String v) {
        if (featOkTst && casFeat_firstMention == null)
      jcas.throwFeatMissing("firstMention", "de.fernunihagen.d2l2.newtypes.CoreferenceEntity");
    ll_cas.ll_setStringValue(addr, casFeatCode_firstMention, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public CoreferenceEntity_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_corefId = jcas.getRequiredFeatureDE(casType, "corefId", "uima.cas.Integer", featOkTst);
    casFeatCode_corefId  = (null == casFeat_corefId) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_corefId).getCode();

 
    casFeat_beginPosition = jcas.getRequiredFeatureDE(casType, "beginPosition", "uima.cas.Integer", featOkTst);
    casFeatCode_beginPosition  = (null == casFeat_beginPosition) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_beginPosition).getCode();

 
    casFeat_endPosition = jcas.getRequiredFeatureDE(casType, "endPosition", "uima.cas.Integer", featOkTst);
    casFeatCode_endPosition  = (null == casFeat_endPosition) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_endPosition).getCode();

 
    casFeat_name = jcas.getRequiredFeatureDE(casType, "name", "uima.cas.String", featOkTst);
    casFeatCode_name  = (null == casFeat_name) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_name).getCode();

 
    casFeat_firstMention = jcas.getRequiredFeatureDE(casType, "firstMention", "uima.cas.String", featOkTst);
    casFeatCode_firstMention  = (null == casFeat_firstMention) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_firstMention).getCode();

  }
}



    