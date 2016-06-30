import Enum from 'enum'
import invariant from 'invariant'

/*
  All the enum should be consistent with their java counterpart
 */

const JOB_STATUS = new Enum(['COMPLETED', 'ERROR', 'RUNNING', 'STARTED'], {
  name: 'JobStatusType',
  ignoreCase: true
})


export function isJobFinished(status) {
  const finished = JOB_STATUS.get('COMPLETED | ERROR')
  return finished.has(JOB_STATUS.get(status))
}

const syncToRepo = 'REPO_SYNC'
const syncToServer = 'SERVER_SYNC';
const JOB_TYPE = new Enum([syncToRepo, syncToServer], {
  name: 'JobType',
  ignoreCase: true
})

export function toJobSummaryKeyName(jobType) {
  const typeEnum = JOB_TYPE.get(jobType);
  if (typeEnum === JOB_TYPE.get(syncToRepo)) {
    return 'syncToRepoJob'
  } else if (typeEnum === JOB_TYPE.get(syncToServer)) {
    return 'syncToTransServerJob'
  }
  invariant(false, `${jobType} is not a valid job type`)
}

export function toJobDescription(jobType) {
  const typeEnum = JOB_TYPE.get(jobType);
  if (typeEnum === JOB_TYPE.get(syncToRepo)) {
    return 'Sync translation to source repo'
  } else if (typeEnum === JOB_TYPE.get(syncToServer)) {
    return 'Sync to Zanata'
  }
  invariant(false, `${jobType} is not a valid job type`)
}
