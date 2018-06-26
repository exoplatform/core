package org.exoplatform.services.organization.externalstore.job;

import org.quartz.*;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.organization.externalstore.IDMExternalStoreImportService;

/**
 * This job imports modified and added IDM entries managed by external store to
 * internal store by adding corresponding queue entry per detected entity.
 */
@DisallowConcurrentExecution
public class IDMEntitiesImportJob implements InterruptableJob {

  private IDMExternalStoreImportService externalStoreImportService;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    try {
      getExternalStoreImportService().importAllModifiedEntitiesToQueue();
    } catch (Exception e) {
      throw new JobExecutionException("An error occurred while executing job IDMEntitiesImportJob", e);
    }
  }

  public IDMExternalStoreImportService getExternalStoreImportService() {
    if (externalStoreImportService == null) {
      externalStoreImportService = ExoContainerContext.getCurrentContainer()
                                                      .getComponentInstanceOfType(IDMExternalStoreImportService.class);
    }
    return externalStoreImportService;
  }

  @Override
  public void interrupt() throws UnableToInterruptJobException {
    getExternalStoreImportService().interrupt();
  }
}
