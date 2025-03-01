import type {ReactNode} from 'react';
import clsx from 'clsx';
import Heading from '@theme/Heading';
import styles from './styles.module.css';

type FeatureItem = {
  title: string;
  image: string;
  description: ReactNode;
};

const FeatureList: FeatureItem[] = [
  {
    title: 'TYPE\nSAFE',
    image: require('@site/static/img/shield.png').default,
    description: (
      <>
        Snitch provides a highly readable and maintainable API for creating web services
        with strong type safety. Define routes, handlers, and parameters with a clean, 
        expressive syntax.
      </>
    ),
  },
  {
    title: 'AUTO\nDOCS',
    image: require('@site/static/img/book.png').default,
    description: (
      <>
        Get complete OpenAPI 3.0 docs with <strong>zero</strong> effort. Every input, output, 
        and response code is automatically documented without any additional setup or annotations.
      </>
    ),
  },
  {
    title: 'MEGA\nFAST',
    image: require('@site/static/img/falcon.png').default,
    description: (
      <>
        Built on established web servers like Undertow, Snitch adds minimal overhead
        while providing a powerful API. No reflection or code generation for production code,
        just pure performance.
      </>
    ),
  },
];

function Feature({title, image, description}: FeatureItem) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center">
        <h3 className={styles.featureTitle}>{title}</h3>
      </div>
      <div className="text--center padding-horiz--md">
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures(): ReactNode {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
