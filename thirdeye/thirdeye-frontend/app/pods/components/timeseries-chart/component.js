import Ember from 'ember';
import c3 from 'c3';
import d3 from 'd3';
import _ from 'lodash';
import moment from 'moment';

export default Ember.Component.extend({
  tagName: 'div',
  classNames: ['timeseries-chart'],

  // internal
  _chart: null,
  _seriesCache: null,

  // external
  series: {
    example_series: {
      timestamps: [0, 1, 2, 5, 6],
      values: [10, 10, 5, 27, 28],
      type: 'line', // 'scatter', 'region'
      color: 'green'
    }
  },

  tooltip: {
    format: {
      title: (d) => moment(d).format('MM/DD hh:mm a'),
      value: (val, ratio, id) => d3.format('.3s')(val)
    }
  },

  legend: {},

  axis: {
    y: {
      show: true
    },
    y2: {
      show: false
    },
    x: {
      type: 'timeseries',
      show: true,
      tick: {
        count: 5,
        format: '%Y-%m-%d'
      }
    }
  },

  subchart: { // on init only
    show: true
  },

  zoom: { // on init only
    enabled: true
  },

  _makeDiffConfig() {
    const cache = this.get('_seriesCache') || {};
    const series = this.get('series') || {};
    const { axis, legend, tooltip } = this.getProperties('axis', 'legend', 'tooltip');

    const addedKeys = Object.keys(series).filter(sid => !cache[sid]);
    const changedKeys = Object.keys(series).filter(sid => cache[sid] && !_.isEqual(cache[sid], series[sid]));
    const deletedKeys = Object.keys(cache).filter(sid => !series[sid]);
    const regionKeys = Object.keys(series).filter(sid => series[sid] && series[sid].type == 'region');

    const regions = regionKeys.map(sid => {
      const t = series[sid].timestamps;
      let region = { start: t[0], end: t[t.length - 1] };

      if ('color' in series[sid]) {
        region.class = `c3-region--${series[sid].color}`;
      }

      return region;
    });

    const unloadKeys = deletedKeys.concat(regionKeys);
    const unload = unloadKeys.concat(unloadKeys.map(sid => `${sid}-timestamps`));

    const loadKeys = addedKeys.concat(changedKeys).filter(sid => !regionKeys.includes(sid));
    const xs = {};
    loadKeys.forEach(sid => xs[sid] = `${sid}-timestamps`);

    const values = loadKeys.map(sid => [sid].concat(series[sid].values));

    const timestamps = loadKeys.map(sid => [`${sid}-timestamps`].concat(series[sid].timestamps));

    const columns = values.concat(timestamps);

    const colors = {};
    loadKeys.filter(sid => series[sid].color).forEach(sid => colors[sid] = series[sid].color);

    const types = {};
    loadKeys.filter(sid => series[sid].type).forEach(sid => types[sid] = series[sid].type);

    const axes = {};
    loadKeys.filter(sid => 'axis' in series[sid]).forEach(sid => axes[sid] = series[sid].axis);

    const config = { unload, xs, columns, types, regions, tooltip, colors, axis, axes, legend };

    return config;
  },

  _makeAxisRange(axis) {
    const range = { min: {}, max: {}};
    Object.keys(axis).filter(key => 'min' in axis[key]).forEach(key => range['min'][key] = axis[key]['min']);
    Object.keys(axis).filter(key => 'max' in axis[key]).forEach(key => range['max'][key] = axis[key]['max']);
    return range;
  },

  _updateCache() {
    const series = this.get('series') || {};
    this.set('_seriesCache', _.cloneDeep(series));
  },

  _updateChart() {
    const diffConfig = this._makeDiffConfig();
    console.log('timeseries-chart: _updateChart(): diffConfig', diffConfig);

    const chart = this.get('_chart');
    chart.regions(diffConfig.regions);
    chart.axis.range(this._makeAxisRange(diffConfig.axis));
    chart.load(diffConfig);
    this._updateCache();
  },

  didUpdateAttrs() {
    this._super(...arguments);
    const series = this.get('series') || {};
    const cache = this.get('cache') || {};

    if (!_.isEqual(series, cache)) {
      Ember.run.debounce(this, this._updateChart, 250);
    }
  },

  didInsertElement() {
    this._super(...arguments);

    const diffConfig = this._makeDiffConfig();
    const config = {};
    config.bindto = this.get('element');
    config.data = {
      xs: diffConfig.xs,
      columns: diffConfig.columns,
      types: diffConfig.types,
      colors: diffConfig.colors,
      axes: diffConfig.axes
    };
    config.axis = diffConfig.axis;
    config.regions = diffConfig.regions;
    config.tooltip = diffConfig.tooltip;
    config.legend = diffConfig.legend;
    config.subchart = this.get('subchart');
    config.zoom = this.get('zoom');

    console.log('timeseries-chart: didInsertElement(): config', config);

    this.set('_chart', c3.generate(config));
    this._updateCache();
  }
});
