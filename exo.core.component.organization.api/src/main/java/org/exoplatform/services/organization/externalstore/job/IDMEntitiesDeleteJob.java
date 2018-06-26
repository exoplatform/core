package org.exoplatform.services.organization.externalstore.job;

import org.quartz.*;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.organization.externalstore.IDMExternalStoreImportService;

/**
 * This is a Job to check deleted entries on external store and that are
 * existing on internal store. When deleted entries are detected, a queue entry
 * will be populated by detected deleted entry.
 */
@DisallowConcurrentExecution
public class IDMEntitiesDeleteJob implements InterruptableJob {

  private IDMExternalStoreImportService externalStoreImportService;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    try {
      getExternalStoreImportService().checkAllEntitiesToDeleteIntoQueue();
    } catch (Exception e) {
      throw new JobExecutionException("An error occurred while executing job IDMEntitiesDeleteJob", e);
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
