/*******************************************************************************
 Copyright © 2014, Oracle and/or its affiliates. All rights reserved.
 
 $revision_history$
 16-dec-2013   Steven Davelaar
 1.1           AdfmfJavaUtilities.getFeatureName() returns id instead of name! Fixed bug
               that this bean does not work as exected when feature id and name are different
 08-jul-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package oracle.ateam.sample.mobile.controller.bean;

import oracle.adfmf.amx.event.ActionEvent;
import oracle.adfmf.framework.api.AdfmfContainerUtilities;
import oracle.adfmf.framework.api.AdfmfJavaUtilities;
import oracle.adfmf.framework.exception.AdfException;

public class GoToFeatureBean
{
  public static final String DATA_SYNCH_FEATURE_ID = "oracle.ateam.sample.mobile.datasynch";
  public String nextFeatureId;
  public String previousFeatureId;
  private boolean dataSynchFeatureVisited = false;
  
  public GoToFeatureBean()
  {
    super();
  }

  public void goToDataSynchFeature(ActionEvent actionEvent)
  {
    setNextFeatureId(DATA_SYNCH_FEATURE_ID);
    // we need to reset data synch feature to seelatest sunch actions. However, when we launch
    // the app. and directly navigate to data sych feature, the app hangs when we do a reset. So,
    // the first time we access the data synch feature, we do not reset, which also slightly 
    // increases performance and reset is not needed anyway the first time
    if (dataSynchFeatureVisited)
    {
      goToNextFeatureAndReset(actionEvent);    
    }
    else
    {
      dataSynchFeatureVisited = true;
      goToNextFeature(actionEvent);      
    }
  }

  public void goToNextFeatureAndReset(ActionEvent actionEvent)
  {
    goToNextFeature(actionEvent);
    AdfmfContainerUtilities.resetFeature(getNextFeatureId());
  }

  public void goToNextFeature(ActionEvent actionEvent)
  {
    if (nextFeatureId==null)
    {
      throw new AdfException("Next feature Id unknown, cannot go to feature",AdfException.ERROR);      
    }
    // method AdfmfJavaUtilities.getFeatureName() returns the ID!!!
//    previousFeatureId = AdfmfContainerUtilities.getFeatureByName(AdfmfJavaUtilities.getFeatureName()).getId();
    previousFeatureId = AdfmfJavaUtilities.getFeatureName();
    AdfmfContainerUtilities.gotoFeature(getNextFeatureId());
  }

  public void goToPreviousFeature(ActionEvent actionEvent)
  {
    if (previousFeatureId==null)
    {
      throw new AdfException("Previous feature Id unknown, cannot go to feature",AdfException.ERROR);      
    }
    AdfmfContainerUtilities.gotoFeature(previousFeatureId);
  }

  public void setNextFeatureId(String featureId)
  {
//    if (AdfmfContainerUtilities.getFeatureById(featureId)==null)
//    {
//      throw new AdfException("Next feature with id "+featureId+" does not exist",AdfException.ERROR);
//    }
    this.nextFeatureId = featureId;
  }

  public String getNextFeatureId()
  {
    return nextFeatureId;
  }

  public String getPreviousFeatureId()
  {
    return previousFeatureId;
  }
}
