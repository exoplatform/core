package org.exoplatform.services.organization.externalstore.job;

import org.quartz.*;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.organization.externalstore.IDMExternalStoreImportService;

/**
 * This Job processes the IDM queue entries that could be of type:
 * - Entity creation
 * - Entity modification
 * - Entity deletion
 */
@DisallowConcurrentExecution
public class IDMQueueProcessorJob implements InterruptableJob {

  private IDMExternalStoreImportService externalStoreImportService;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    try {
      getExternalStoreImportService().processQueueEntries();
    } catch (Exception e) {
      throw new JobExecutionException("An error occurred while executing job IDMQueueProcessorJob", e);
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
