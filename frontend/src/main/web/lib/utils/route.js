
export function redirectToSignIn(router) {
  console.info('redirect to home page for sign in')
  router.push({
    pathname: '/',
    // query: { modal: true },
    // TODO check this state in home page and display a message
    state: { needSignIn: true }
  })
}
