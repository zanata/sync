import React from 'react'
import WorkSummary from './WorkSummary'

const {PropTypes} = React

export default React.createClass({
  propTypes: {
    workSummaries: PropTypes.arrayOf(PropTypes.object).isRequired,
    zanataUsername: PropTypes.string,
    loadWorkSummaries: PropTypes.func.isRequired,
    runJob: PropTypes.func.isRequired,
    runningJobs: PropTypes.object.isRequired
  },

  // ask for `router` from context
  contextTypes: {
    router: React.PropTypes.object
  },

  componentWillMount() {
    // TODO load data
    const {zanataUsername, loadWorkSummaries} = this.props
    if (zanataUsername) {
      loadWorkSummaries(zanataUsername)
    } else {
      // TODO use props not Configs
      // TODO dup in WorkForm
      const path = `${Configs.basename}`;
      console.info('redirect to home page for sign in:' + path)
      this.context.router.push({
        pathname: path,
        // query: { modal: true },
        // TODO check this state in home page and display a message
        state: { needSignIn: true }
      })
    }
  },

  componentWillUnmount() {
    // TODO discard loaded data? do we need to as we are using props not state? https://github.com/reactjs/react-router/blob/master/docs/guides/ComponentLifecycle.md

  },

  render() {
    const {runJob, workSummaries, runningJobs} = this.props
    const summaries = workSummaries.map(work => {
      return (
        <WorkSummary key={work.id} id={work.id} name={work.name}
          description={work.description}
          syncToRepoJob={work.syncToRepoJob}
          syncToTransServerJob={work.syncToTransServerJob}
          runJob={runJob} runningJobs={runningJobs}
          {...this.props}
        />
      )
    })
    return (
      <div className='row row-cards-pf'>
        {summaries}
      </div>
    )
  }
})
