# Main Puppet Node Manifest: site.pp
# Target Environment: Puppet-Provisioned Fitness Equipment Maintenance Portal

node default {
  class { 'fitness_portal':
    version      => '1.0.0-mvp',
    port         => 8080,
    service_user => 'fitness',
    install_dir  => '/opt/fitness-portal',
    jvm_max_mem  => '256m',
  }
}

class fitness_portal (
  String $version      = '1.0.0-mvp',
  Integer $port        = 8080,
  String $service_user = 'fitness',
  String $install_dir  = '/opt/fitness-portal',
  String $jvm_max_mem  = '256m',
) {
  # 1. User & Group Management
  group { 'fitnessgroup':
    ensure => present,
    gid    => 1001,
  }

  user { $service_user:
    ensure     => present,
    uid        => 1001,
    gid        => 'fitnessgroup',
    home       => $install_dir,
    shell      => '/bin/false',
    managehome => false,
    require    => Group['fitnessgroup'],
  }

  # 2. Package Prerequisites
  $prereq_packages = ['openjdk-17-jdk-headless', 'curl', 'wget']
  package { $prereq_packages:
    ensure => installed,
  }

  # 3. Directory Structure
  $directories = [
    $install_dir,
    "${install_dir}/bin",
    "${install_dir}/config",
    "${install_dir}/data",
    "${install_dir}/logs",
  ]

  file { $directories:
    ensure  => directory,
    owner   => $service_user,
    group   => 'fitnessgroup',
    mode    => '0750',
    require => User[$service_user],
  }

  # 4. Configuration Specification File
  file { "${install_dir}/config/application.properties":
    ensure  => file,
    owner   => $service_user,
    group   => 'fitnessgroup',
    mode    => '0640',
    content => epp('fitness_portal/application.properties.epp', {
      'port'        => $port,
      'environment' => 'production',
      'version'     => $version,
      'jvm_max_mem' => $jvm_max_mem,
    }),
    require => File["${install_dir}/config"],
    notify  => Service['fitness-portal'],
  }

  # 5. Application Binary (JAR deployment)
  file { "${install_dir}/bin/fitness-portal.jar":
    ensure  => file,
    source  => 'puppet:///modules/fitness_portal/fitness-portal.jar',
    owner   => $service_user,
    group   => 'fitnessgroup',
    mode    => '0750',
    require => File["${install_dir}/bin"],
    notify  => Service['fitness-portal'],
  }

  # 6. Systemd Service Unit Definition
  file { '/etc/systemd/system/fitness-portal.service':
    ensure  => file,
    owner   => 'root',
    group   => 'root',
    mode    => '0644',
    content => epp('fitness_portal/fitness-portal.service.epp', {
      'service_user' => $service_user,
      'install_dir'  => $install_dir,
      'port'         => $port,
      'jvm_max_mem'  => $jvm_max_mem,
    }),
    notify  => Service['fitness-portal'],
  }

  # 7. Service Daemon State
  service { 'fitness-portal':
    ensure     => running,
    enable     => true,
    hasrestart => true,
    hasstatus  => true,
    require    => [
      File['/etc/systemd/system/fitness-portal.service'],
      File["${install_dir}/bin/fitness-portal.jar"],
      Package['openjdk-17-jdk-headless'],
    ],
  }
}
