var webpack = require('webpack')
var path = require('path')
var ExtractTextPlugin = require('extract-text-webpack-plugin')

// bundle destination (default is current directory)
var bundleDest = process.env.npm_config_env_bundleDest || __dirname;

module.exports = {
  context: __dirname,
  entry: [
    './index'
  ],
  output: {
    path: bundleDest,
    filename: 'frontend.bundle.min.js',
    pathinfo: true
  },
  module: {
    loaders: [
      {
        test: /\.jsx?$/,
        exclude: /node_modules/,
        loaders: ['babel-loader?presets[]=es2015&presets[]=react&presets[]=stage-0']
      },
      {
        test: /\.css$/,
        loader: ExtractTextPlugin.extract('style-loader', 'css-loader?safe')
      },
      {
        test: /\.woff(2)?(\?v=[0-9]\.[0-9]\.[0-9])?$/,
        loader: "url-loader?limit=10000&minetype=application/font-woff"
      },
      {
        test: /\.(gif|jpg|ttf|eot|svg)(\?v=[0-9]\.[0-9]\.[0-9])?$/,
        loader: "file-loader"
      }
    ]
  },
  plugins: [
    new webpack.DefinePlugin({ "global.GENTLY": false }),
    new webpack.IgnorePlugin(/^\.\/locale$/, /moment$/),
    new ExtractTextPlugin('bundle.css'),
    new webpack.optimize.UglifyJsPlugin({
        compress: {
            warnings: false
        }
    }),
    new webpack.optimize.DedupePlugin(),
    new webpack.DefinePlugin({
      "process.env": {
        NODE_ENV: JSON.stringify("production")
      }
    }),
    new webpack.NoErrorsPlugin()
  ],
  resolve: {
    extensions: ['', '.js', '.jsx', '.json', '.css']
  },
  node: {
    __dirname: true
  }
};
