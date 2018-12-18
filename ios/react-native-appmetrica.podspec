require 'json'
package = JSON.parse(File.read(File.join(__dir__, '..', 'package.json')))

Pod::Spec.new do |s|
  s.name         = package['name']
  s.version      = package['version']
  s.summary      = package['description']
  s.author       = package['author']

  s.homepage     = package['homepage']

  s.license      = { :type => package['license'] }
  s.platform     = :ios, "8.0"

  s.source       = { :git => "https://github.com/doochik/react-native-appmetrica.git", :tag => "v#{s.version}" }
  s.source_files = "RCTAppMetrica/**/*.{h,m}"
  s.dependency "YandexMobileMetrica"
end
