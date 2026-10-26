import { test } from 'node:test'
import { strict as assert } from 'node:assert'
import { distanceMeters, identifySite } from '../src/geo.js'

const a = {siteId:1, name:'A', latitude:10.778, longitude:106.698, radiusMeters:140}
const b = {siteId:2, name:'B', latitude:10.779, longitude:106.698, radiusMeters:110}
const fix = (latitude, longitude, accuracy=10) => ({latitude,longitude,accuracy})

test('distance identical coordinate = 0', () => assert.equal(distanceMeters(a,a),0))
test('distance between two sites approximately 111m', () => {
  assert.ok(distanceMeters(a,b)>100 && distanceMeters(a,b)<120)
})
test('entering a site needs two successive fixes', () => {
  const pos=fix(a.latitude,a.longitude)
  let r=identifySite(pos,[a,b],null,null)
  assert.equal(r.site,null)
  r=identifySite(pos,[a,b],r.site,r.candidate)
  assert.equal(r.site.siteId,1)
})
test('switch to adjacent site if distinctly closer', () => {
  const pos=fix(b.latitude,b.longitude)
  let r=identifySite(pos,[a,b],a,null)
  assert.equal(r.site.siteId,1)
  r=identifySite(pos,[a,b],r.site,r.candidate)
  assert.equal(r.site.siteId,2)
})
test('GPS with high inaccuracy should not change site', () => {
  const r=identifySite(fix(b.latitude,b.longitude,300),[a,b],a,null)
  assert.equal(r.site.siteId,1)
})
test('no nearby site clears current once outside exit buffer', () => {
  const r=identifySite(fix(10.8,106.8),[a,b],a,null)
  assert.equal(r.site,null)
})
