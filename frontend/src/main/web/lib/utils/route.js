import Configs from '../constants/Configs'

export function redirectToSignIn(router) {
  const path = `${Configs.basename}`;
  console.info('redirect to home page for sign in:' + path)
  router.push({
    pathname: path,
    // query: { modal: true },
    // TODO check this state in home page and display a message
    state: { needSignIn: true }
  })
}